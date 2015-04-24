package com.infusion.jirareconciler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.infusion.jirareconciler.base.BaseFragment;
import com.infusion.jirareconciler.jira.Board;
import com.infusion.jirareconciler.jira.BoardDetails;
import com.infusion.jirareconciler.reconciliation.IssueIdDecoder;
import com.infusion.jirareconciler.reconciliation.Reconciler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class CaptureFragment extends BaseFragment {
    private static final Logger LOG = LoggerFactory.getLogger(CaptureFragment.class);
    public static final String EXTRA_BOARD_DETAILS = "com.infusion.jirareconciler.board_details";
    public static final String EXTRA_BOARD = "com.infusion.jirareconciler.board";
    public static final String EXTRA_RECONCILIATION = "com.infusion.jirareconciler.reconciliation";
    private Camera camera;
    private ProgressDialog progressDialog;
    private int currentLane;
    private Board board;
    private BoardDetails boardDetails;
    private Reconciler reconciler;

    @InjectView(R.id.lane_camera_surface_view) SurfaceView surfaceView;
    @InjectView(R.id.crop_left) View cropLeft;
    @InjectView(R.id.crop_right) View cropRight;
    @InjectView(R.id.crop_size_bar) SeekBar cropSizeBar;

    public static CaptureFragment newInstance(Board board, BoardDetails boardDetails) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_BOARD, board);
        args.putSerializable(EXTRA_BOARD_DETAILS, boardDetails);

        CaptureFragment fragment = new CaptureFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        board = (Board) getArguments().getSerializable(EXTRA_BOARD);
        boardDetails = (BoardDetails) getArguments().getSerializable(EXTRA_BOARD_DETAILS);
        reconciler = new Reconciler(board, boardDetails);

        updateCurrentLane();
    }

    private void updateCurrentLane() {
        getActivity().setTitle(boardDetails.getLanes()[currentLane]);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_capture, null);
        ButterKnife.inject(this, view);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        cropSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateCropSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (camera != null) {
                        camera.setPreviewDisplay(holder);
                    }
                } catch (IOException e) {
                    LOG.error("Error setting up preview display", e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (camera == null) {
                    return;
                }

                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestSupportedSize(parameters.getSupportedPreviewSizes(), null);
                parameters.setPreviewSize(size.width, size.height);
                size = getBestSupportedSize(parameters.getSupportedPictureSizes(), size);
                parameters.setPictureSize(size.width, size.height);
                camera.setParameters(parameters);
                try {
                    camera.startPreview();
                } catch (Exception e) {
                    LOG.error("Could not start preview", e);
                    camera.release();
                    camera = null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (camera != null) {
                    camera.stopPreview();
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        camera = Camera.open(0);
        camera.setDisplayOrientation(90);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @OnClick(R.id.lane_camera_focus_trigger)
    public void autoFocus() {
        if (camera == null) {
            return;
        }

        camera.autoFocus(null);
    }

    @OnClick(R.id.lane_camera_trigger)
    public void capture() {
        if (camera == null) {
            return;
        }

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (!success) {
                    return;
                }

                camera.takePicture(
                        new Camera.ShutterCallback() {
                            @Override
                            public void onShutter() {
                                progressDialog.setMessage(getString(R.string.analyzing_lane));
                                progressDialog.show();
                            }
                        },
                        null,
                        new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, final Camera camera) {
                                int cropPercentage = cropLeft.getWidth() * 100 / surfaceView.getWidth();
                                final String[] issueIds = IssueIdDecoder.decode(data, cropPercentage);

                                progressDialog.dismiss();

                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                                alertDialog.setTitle(R.string.lane_captured);
                                alertDialog.setMessage(getString(R.string.found_issues, issueIds.length));
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.next),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                                reconciler.addLane(boardDetails.getLanes()[currentLane], issueIds);

                                                currentLane++;

                                                if (currentLane >= boardDetails.getLanes().length) {
                                                    Intent intent = new Intent();
                                                    intent.putExtra(EXTRA_RECONCILIATION, reconciler.reconcile());
                                                    getActivity().setResult(Activity.RESULT_OK, intent);
                                                    getActivity().finish();
                                                }
                                                else {
                                                    updateCurrentLane();
                                                    camera.startPreview();
                                                }
                                            }
                                        });
                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.retry),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                camera.startPreview();
                                            }
                                        });
                                alertDialog.show();
                            }
                        }
                );
            }
        });
    }

    private void updateCropSize(int progress) {
        ViewGroup.LayoutParams leftParams = cropLeft.getLayoutParams();
        ViewGroup.LayoutParams rightParams = cropRight.getLayoutParams();

        if (progress == 0) {
            leftParams.width = rightParams.width = 0;
        }
        else if (progress == 100) {
            leftParams.width = rightParams.width = surfaceView.getWidth() / 2;
        }
        else {
            leftParams.width = rightParams.width = surfaceView.getWidth() * progress / 200;
        }

        cropLeft.setLayoutParams(leftParams);
        cropRight.setLayoutParams(rightParams);

        cropLeft.invalidate();
        cropRight.invalidate();
    }

    private Camera.Size getBestSupportedSize(List<Camera.Size> supportedPreviewSizes, Camera.Size targetRatio) {
        final double ASPECT_TOLERANCE = 0.05;

        Camera.Size bestSize = null;
        int largestArea = 0;
        for (Camera.Size size : supportedPreviewSizes) {
            double aspectRatio = (double)size.width / size.height;
            if (targetRatio != null && Math.abs(((double) targetRatio.width / targetRatio.height) - aspectRatio) > ASPECT_TOLERANCE) {
                continue;
            }

            int area = size.width * size.height;
            if (area > largestArea) {
                bestSize = size;
                largestArea = area;
            }
        }

        if (bestSize == null) {
            return getBestSupportedSize(supportedPreviewSizes, null);
        }

        return bestSize;
    }
}
