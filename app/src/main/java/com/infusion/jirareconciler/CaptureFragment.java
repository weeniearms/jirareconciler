package com.infusion.jirareconciler;

import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import java.io.IOException;
import java.util.List;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class CaptureFragment extends Fragment {
    private static final String TAG = "CaptureFragment";
    public static final String EXTRA_BOARD_DETAILS = "com.infusion.jirareconciler.board_details";
    public static final String EXTRA_BOARD = "com.infusion.jirareconciler.board";
    private SurfaceView surfaceView;
    private Camera camera;
    private View cropLeft;
    private View cropRight;
    private SeekBar cropSizeBar;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_capture, null);

        cropLeft = view.findViewById(R.id.crop_left);
        cropRight = view.findViewById(R.id.crop_right);
        cropSizeBar = (SeekBar) view.findViewById(R.id.crop_size_bar);
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

        surfaceView = (SurfaceView) view.findViewById(R.id.lane_camera_surface_view);
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
                    Log.e(TAG, "Error setting up preview display", e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (camera == null) {
                    return;
                }

                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestSupportedSize(parameters.getSupportedPreviewSizes());
                parameters.setPreviewSize(size.width, size.height);
                size = getBestSupportedSize(parameters.getSupportedPictureSizes());
                parameters.setPictureSize(size.width, size.height);
                camera.setParameters(parameters);
                try {
                    camera.startPreview();
                }
                catch (Exception e) {
                    Log.e(TAG, "Could not start preview", e);
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

    private Camera.Size getBestSupportedSize(List<Camera.Size> supportedPreviewSizes) {
        Camera.Size bestSize = supportedPreviewSizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size size : supportedPreviewSizes) {
            int area = size.width * size.height;
            if (area > largestArea) {
                bestSize = size;
                largestArea = area;
            }
        }

        return bestSize;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            camera = Camera.open(0);
        }
        else {
            camera = Camera.open();
        }

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
}
