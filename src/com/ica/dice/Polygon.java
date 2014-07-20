package com.ica.dice;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class Polygon extends View {

    private int sides = 2;
    private int strokeColor = 0xff000000;
    private int strokeWidth = 0;
    private int fillColor = 0xffffffff;
    private float startAngle = -90;
    private boolean showInscribedCircle = false;
    private float fillPercent = 1;
    private int fillBitmapResourceId = -1;
    private String text = "";
    private int textColor = 0xff000000;
    private int textSize = 20;

    private Paint fillPaint;
    private Paint strokePaint;
    private Paint inscribedCirclePaint;
    private Paint textPaint;

    private Path polyPath;

    public Polygon(Context context) {
        super(context);
    }

    public Polygon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public Polygon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private void init(AttributeSet attrs){
        TypedArray polyAttributes =getContext().obtainStyledAttributes(
                attrs,
                R.styleable.Polygon);

        sides = polyAttributes.getInt(R.styleable.Polygon_sides, sides);
        strokeColor = polyAttributes.getColor(R.styleable.Polygon_stroke_color, strokeColor);
        strokeWidth = polyAttributes.getInt(R.styleable.Polygon_stroke_width, strokeWidth);
        fillColor = polyAttributes.getColor(R.styleable.Polygon_fill_color, fillColor);
        startAngle = polyAttributes.getFloat(R.styleable.Polygon_start_angle, startAngle);
        showInscribedCircle = polyAttributes.getBoolean(R.styleable.Polygon_inscribed_circle, showInscribedCircle);
        fillBitmapResourceId = polyAttributes.getResourceId(R.styleable.Polygon_fill_bitmap, fillBitmapResourceId);
        text = polyAttributes.getString(R.styleable.Polygon_text);
        textColor = polyAttributes.getColor(R.styleable.Polygon_text_color, textColor);
        textSize = polyAttributes.getInt(R.styleable.Polygon_text_size, textSize);

        float fillPct = polyAttributes.getFloat(R.styleable.Polygon_fill_percent, 100);

        polyAttributes.recycle();

        fillPaint = new Paint();
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);

        if(fillBitmapResourceId != -1){
            Bitmap fillBitmap = BitmapFactory.decodeResource(getResources(), fillBitmapResourceId);
            BitmapShader fillShader = new BitmapShader(fillBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            fillPaint.setShader(fillShader);
        }

        if(strokeWidth > 0){
            strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            strokePaint.setColor(strokeColor);
            strokePaint.setStrokeWidth(strokeWidth);
            strokePaint.setStyle(Paint.Style.STROKE);
        }

        if(showInscribedCircle){
            inscribedCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            inscribedCirclePaint.setColor(0xff000000);
            inscribedCirclePaint.setStrokeWidth(1);
            inscribedCirclePaint.setStyle(Paint.Style.STROKE);
        }

        polyPath = new Path();
        polyPath.setFillType(Path.FillType.WINDING);

        if(fillPct < 100){
            fillPercent = fillPct / 100;
        }

        if (fillPercent < 0 || fillPercent > 100){
            fillPercent = 1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measureWidth(widthMeasureSpec);
        int measuredHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private int measureWidth(int measureSpec){
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result;

        switch(specMode){
            case MeasureSpec.AT_MOST:
                result = specSize;
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;

            default:
                // random size if nothing is specified
                result = 500;
                break;
        }
        return result;
    }

    private int measureHeight(int measureSpec){
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result;

        switch(specMode){
            case MeasureSpec.AT_MOST:
                result = specSize;
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;

            default:
                // random size if nothing is specified
                result = 500;
                break;
        }
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int x = (measuredWidth/2)  ;
        int y = (measuredHeight/2) ;
        int radius = Math.min(x,y) ;

        if (sides < 3) return;

        float a = (float) (Math.PI * 2)/sides;
        int workingRadius = radius;
        polyPath.reset();

        // The poly is created as a shape in a path.
        // If there is a hole in the poly, draw a 2nd shape inset from the first
        for(int j = 0; j < ((fillPercent < 1) ? 2 : 1) ; j++){
            polyPath.moveTo(workingRadius,0);
            for (int i = 1; i < sides; i++) {
                polyPath.lineTo((float)(workingRadius*Math.cos(a*i)),
                        (float)(workingRadius*Math.sin(a*i)));
            }
            polyPath.close();

            workingRadius -= radius * fillPercent;
            a = -a;
        }

        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(startAngle);
        canvas.drawPath(polyPath, fillPaint);

        canvas.restore();

        if (this.text != null) {
            //Paint.FontMetrics fm = textPaint.getFontMetrics();
            //float tx = x - (fm.ascent + fm.descent)/2;
            //float ty = y - (fm.ascent + fm.descent)/2;

            android.graphics.Rect bounds = new android.graphics.Rect();
            textPaint.getTextBounds(text, 0, text.length(), bounds);
            float tx = x - (textSize/4) - 3;//(bounds.width() / 2);
            float ty = y + (textSize/4) + 3;//(bounds.height() / 2);
            canvas.drawText(this.text, tx, ty, textPaint);
        }

        if(showInscribedCircle){
            canvas.drawCircle(x,y,radius, inscribedCirclePaint);
        }
        super.onDraw(canvas);
    }
}
