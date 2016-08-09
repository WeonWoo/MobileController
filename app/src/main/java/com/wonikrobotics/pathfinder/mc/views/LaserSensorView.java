package com.wonikrobotics.pathfinder.mc.views;

/**
 * Created by Felix on 2016-08-03.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Notebook on 2016-07-13.
 */
public abstract class LaserSensorView extends View {
    public static final int AROUND_ROBOT = 1;
    public static final int FRONT_OF_ROBOT = 2;
    public static final int FILL_INSIDE = 3;
    public static final int FILL_OUTSIDE = 4;
    public static final int POINT_CLOUD = 5;
    public static final int POINT_CLOUD_FILL_INSIDE = 6;
    public static final int POINT_CLOUD_FILL_OUTSIDE = 7;
    private sensor_msgs.LaserScan scan_msg;
    private float max_val = 0;
    private float width, height, radius;
    private boolean mode = false;
    private float old_dist = 1f;
    private Paint paint = new Paint();
    private Paint line = new Paint();
    private Paint laser = new Paint();
    private Paint point = new Paint();
    private PointF near = null, far = null;
    private OnAutoResizeChangeListener resizeChangeListener = null;
    private boolean autoResizing = true;
    private int currentRange = 1;
    private int currentDisplay = 3;

    public LaserSensorView(Context c) {
        super(c);
        paint.setColor(Color.BLACK);
        paint.setTextSize(40f);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2);
        line.setColor(Color.BLACK);
        line.setAlpha(50);
        laser.setColor(Color.RED);
        laser.setAlpha(70);
        laser.setStrokeWidth(3);
        point.setColor(Color.BLUE);
        point.setAlpha(70);
        point.setStrokeWidth(10);
    }

    public void setAutoResizing(boolean tf) {
        this.autoResizing = tf;
    }

    public void update(sensor_msgs.LaserScan nm) {
        if (nm != null) {
            this.scan_msg = nm;
            if (max_val == 0)
                max_val = scan_msg.getRangeMax();
            if (autoResizing) {
                float newMax = 0f;
                for (float range : nm.getRanges()) {
                    if (newMax < range)
                        newMax = range;
                }
                max_val = newMax + 1;
            }
            this.onMaxValChanged(max_val);
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (this.scan_msg != null) {
            if (width == 0 && height == 0) {
                width = canvas.getWidth();
                height = canvas.getHeight();
                if (currentRange == AROUND_ROBOT) {
                    if (width > height)
                        radius = height / 2f;
                    else
                        radius = width / 2f;
                } else if (currentRange == FRONT_OF_ROBOT) {
                    radius = width / 2f;
                }
            }
            if (currentRange == AROUND_ROBOT) {
                canvas.drawCircle(width / 2f, height / 2f, radius * 1.0f, line);
                canvas.drawCircle(width / 2f, height / 2f, radius * 0.8f, line);
                canvas.drawCircle(width / 2f, height / 2f, radius * 0.6f, line);
                canvas.drawCircle(width / 2f, height / 2f, radius * 0.4f, line);
                canvas.drawCircle(width / 2f, height / 2f, radius * 0.2f, line);
                canvas.drawText(Integer.toString(Math.round(max_val * 1.0f)), width / 2f - (radius * 1.0f), height / 2f, paint);
                canvas.drawText(Integer.toString(Math.round(max_val * 0.8f)), width / 2f - (radius * 0.8f), height / 2f, paint);
                canvas.drawText(Integer.toString(Math.round(max_val * 0.6f)), width / 2f - (radius * 0.6f), height / 2f, paint);
                canvas.drawText(Integer.toString(Math.round(max_val * 0.4f)), width / 2f - (radius * 0.4f), height / 2f, paint);
                canvas.drawText(Integer.toString(Math.round(max_val * 0.2f)), width / 2f - (radius * 0.2f), height / 2f, paint);
            } else if (currentRange == FRONT_OF_ROBOT) {
                canvas.drawCircle(width / 2f, height, radius * 1.0f, line);
                canvas.drawCircle(width / 2f, height, radius * 0.8f, line);
                canvas.drawCircle(width / 2f, height, radius * 0.6f, line);
                canvas.drawCircle(width / 2f, height, radius * 0.4f, line);
                canvas.drawCircle(width / 2f, height, radius * 0.2f, line);
                canvas.drawText(Integer.toString(Math.round(max_val * 1.0f)), width / 2f, height - (radius * 1.0f), paint);
                canvas.drawText(Integer.toString(Math.round(max_val * 0.8f)), width / 2f, height - (radius * 0.8f), paint);
                canvas.drawText(Integer.toString(Math.round(max_val * 0.6f)), width / 2f, height - (radius * 0.6f), paint);
                canvas.drawText(Integer.toString(Math.round(max_val * 0.4f)), width / 2f, height - (radius * 0.4f), paint);
                canvas.drawText(Integer.toString(Math.round(max_val * 0.2f)), width / 2f, height - (radius * 0.2f), paint);
            }


            float angle = scan_msg.getAngleMin();

            switch (currentDisplay) {
                case FILL_INSIDE:
                    float[] lineEndPoints = new float[scan_msg.getRanges().length * 4];
                    int numEndPoints = 0;
                    for (float range : scan_msg.getRanges()) {
                        // Only process ranges which are in the valid range.

                        if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                            if (near == null) {
                                if (currentRange == AROUND_ROBOT) {
                                    near = new PointF(width / 2f, height / 2f);
                                } else if (currentRange == FRONT_OF_ROBOT) {
                                    near = new PointF(width / 2f, height);
                                }
                            }
                            far = null;
                            if (currentRange == AROUND_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height - (float) Math.cos(angle) * (radius) * (range / max_val));
                            }
                            if (far != null) {
                                lineEndPoints[numEndPoints++] = near.x;
                                lineEndPoints[numEndPoints++] = near.y;
                                lineEndPoints[numEndPoints++] = far.x;
                                lineEndPoints[numEndPoints++] = far.y;
                            }
                        }
                        angle += scan_msg.getAngleIncrement();
                    }
                    canvas.drawLines(lineEndPoints, 0, numEndPoints, laser);
                    break;
                case FILL_OUTSIDE:
                    float[] lineEndPoints2 = new float[scan_msg.getRanges().length * 4];
                    int numEndPoints2 = 0;
                    for (float range : scan_msg.getRanges()) {
                        // Only process ranges which are in the valid range.
                        if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                            far = null;
                            near = null;
                            if (currentRange == AROUND_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius),
                                        height / 2f - (float) Math.cos(angle) * (radius));
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius),
                                        height - (float) Math.cos(angle) * (radius));
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height - (float) Math.cos(angle) * (radius) * (range / max_val));
                            }
                            if (near != null) {
                                lineEndPoints2[numEndPoints2++] = near.x;
                                lineEndPoints2[numEndPoints2++] = near.y;
                                lineEndPoints2[numEndPoints2++] = far.x;
                                lineEndPoints2[numEndPoints2++] = far.y;
                            }
                        }
                        angle += scan_msg.getAngleIncrement();
                    }
                    canvas.drawLines(lineEndPoints2, 0, numEndPoints2, laser);
                    break;
                case POINT_CLOUD:
                    float[] lineEndPoints3 = new float[scan_msg.getRanges().length * 2];
                    int numEndPoints3 = 0;
                    for (float range : scan_msg.getRanges()) {
                        // Only process ranges which are in the valid range.
                        if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                            near = null;
                            if (currentRange == AROUND_ROBOT) {
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height - (float) Math.cos(angle) * (radius) * (range / max_val));
                            }
                            if (near != null) {
                                lineEndPoints3[numEndPoints3++] = near.x;
                                lineEndPoints3[numEndPoints3++] = near.y;
                            }
                        }
                        angle += scan_msg.getAngleIncrement();
                    }
                    canvas.drawPoints(lineEndPoints3, 0, numEndPoints3, point);
                    break;
                case POINT_CLOUD_FILL_INSIDE:
                    int size = scan_msg.getRanges().length;
                    float[] lineEndPoints4 = new float[size * 4];
                    float[] cloudEndPoints = new float[size * 2];
                    int numLinePoints = 0;
                    int numCloudPoints = 0;
                    for (float range : scan_msg.getRanges()) {
                        // Only process ranges which are in the valid range.

                        if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                            if (near == null) {
                                if (currentRange == AROUND_ROBOT) {
                                    near = new PointF(width / 2f, height / 2f);
                                } else if (currentRange == FRONT_OF_ROBOT) {
                                    near = new PointF(width / 2f, height);
                                }
                            }
                            far = null;
                            if (currentRange == AROUND_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height - (float) Math.cos(angle) * (radius) * (range / max_val));
                            }
                            if (far != null) {
                                lineEndPoints4[numLinePoints++] = near.x;
                                lineEndPoints4[numLinePoints++] = near.y;
                                lineEndPoints4[numLinePoints++] = far.x;
                                lineEndPoints4[numLinePoints++] = far.y;
                                cloudEndPoints[numCloudPoints++] = far.x;
                                cloudEndPoints[numCloudPoints++] = far.y;
                            }
                        }
                        angle += scan_msg.getAngleIncrement();
                    }
                    canvas.drawLines(lineEndPoints4, 0, numLinePoints, laser);
                    canvas.drawPoints(cloudEndPoints, 0, numCloudPoints, point);
                    break;
                case POINT_CLOUD_FILL_OUTSIDE:
                    int size2 = scan_msg.getRanges().length;
                    float[] lineEndPoints5 = new float[size2 * 4];
                    float[] cloudEndPoints2 = new float[size2 * 2];
                    int numLinePoints2 = 0;
                    int numCloudPoints2 = 0;
                    for (float range : scan_msg.getRanges()) {
                        // Only process ranges which are in the valid range.

                        if (scan_msg.getRangeMin() <= range && range <= scan_msg.getRangeMax()) {
                            far = null;
                            near = null;
                            if (currentRange == AROUND_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius),
                                        height / 2f - (float) Math.cos(angle) * (radius));
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height / 2f - (float) Math.cos(angle) * (radius) * (range / max_val));
                            } else if (currentRange == FRONT_OF_ROBOT) {
                                far = new PointF((width / 2f) - (float) Math.sin(angle) * (radius),
                                        height - (float) Math.cos(angle) * (radius));
                                near = new PointF((width / 2f) - (float) Math.sin(angle) * (radius) * (range / max_val),
                                        height - (float) Math.cos(angle) * (radius) * (range / max_val));
                            }
                            if (far != null) {
                                lineEndPoints5[numLinePoints2++] = near.x;
                                lineEndPoints5[numLinePoints2++] = near.y;
                                lineEndPoints5[numLinePoints2++] = far.x;
                                lineEndPoints5[numLinePoints2++] = far.y;
                                cloudEndPoints2[numCloudPoints2++] = near.x;
                                cloudEndPoints2[numCloudPoints2++] = near.y;
                            }
                        }
                        angle += scan_msg.getAngleIncrement();
                    }
                    canvas.drawLines(lineEndPoints5, 0, numLinePoints2, laser);
                    canvas.drawPoints(cloudEndPoints2, 0, numCloudPoints2, point);
                    break;
            }
        }
    }

    public void setDisplayRangeMode(int mode) {
        this.currentRange = mode;
    }

    public void setDiplayMode(int mode) {
        this.currentDisplay = mode;
    }

    public void setLaserPaint(Paint p) {
        this.laser = p;
    }

    public void setPointPaint(Paint p) {
        this.point = p;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:    //첫번째 손가락 터치
                break;
            case MotionEvent.ACTION_MOVE:   // 드래그 중이면, 이미지의 X,Y값을 변환시키면서 위치 이동.
                if (mode == true && e.getPointerCount() >= 2) {    // 핀치줌 중이면, 이미지의 거리를 계산해서 확대를 한다.
                    float dist = spacing(e);
                    if (dist - old_dist > 20) {  // zoom in
                        max_val = max_val * 0.9f;
                        this.onMaxValChanged(max_val);
                    } else if (old_dist - dist > 20) {  // zoom out
                        max_val = max_val * 1.1f;
                        this.onMaxValChanged(max_val);
                    }
                    old_dist = dist;
                }
                break;
            case MotionEvent.ACTION_UP:    // 첫번째 손가락을 떼었을 경우
            case MotionEvent.ACTION_POINTER_UP:  // 두번째 손가락을 떼었을 경우
                mode = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: //두번째 손가락을 터치한 경우
                mode = true;
                autoResizing = false;
                if (resizeChangeListener != null)
                    resizeChangeListener.onChange(autoResizing);
                old_dist = spacing(e);
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("touch", "cancel");
            default:
                break;

        }
        return true;
    }

    public void setOnAutoResizeChangeListener(OnAutoResizeChangeListener listener) {
        this.resizeChangeListener = listener;
    }

    public void clearOnAutoResizeChangeListner() {
        this.resizeChangeListener = null;
    }

    public abstract void onMaxValChanged(float val);

    private float spacing(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);

            return (float) Math.sqrt(x * x + y * y);
        }
        return 0;

    }

    public static abstract class OnAutoResizeChangeListener {
        public abstract void onChange(boolean onOff);
    }

}