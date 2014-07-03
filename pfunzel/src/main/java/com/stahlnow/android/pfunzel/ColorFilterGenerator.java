package com.stahlnow.android.pfunzel;

import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

public class ColorFilterGenerator
{
    /**
     * Creates a HUE ajustment ColorFilter
     * @see http://groups.google.com/group/android-developers/browse_thread/thread/9e215c83c3819953
     * @see http://gskinner.com/blog/archives/2007/12/colormatrix_cla.html
     * @param value degrees to shift the hue.
     * @return
     */
    public static ColorFilter adjustHue(float hue, float saturation)
    {
        ColorMatrix cm = new ColorMatrix();


        cm.setSaturation(saturation);
        final float m[] = cm.getArray();
        final float c = 3.0f;               // contrast
        final float bright = 1.0f;          // brightness

        cm.set(new float[] {
                m[ 0] * c, m[ 1] * c, m[ 2] * c, m[ 3] * c, m[ 4] * c + bright,
                m[ 5] * c, m[ 6] * c, m[ 7] * c, m[ 8] * c, m[ 9] * c + bright,
                m[10] * c, m[11] * c, m[12] * c, m[13] * c, m[14] * c + bright,
                m[15]    , m[16]    , m[17]    , m[18]    , m[19] });

        //cm.setScale(2.0f, 2.0f, 2.0f, 1.0f);


        adjustHue(cm, hue);

        return new ColorMatrixColorFilter(cm);
    }

    /**
     * @see http://groups.google.com/group/android-developers/browse_thread/thread/9e215c83c3819953
     * @see http://gskinner.com/blog/archives/2007/12/colormatrix_cla.html
     * @param cm
     * @param value
     */
    public static void adjustHue(ColorMatrix cm, float value)
    {
        value = cleanValue(value, 180f) / 180f * (float) Math.PI;
        if (value == 0)
        {
            return;
        }
        float cosVal = (float) Math.cos(value);
        float sinVal = (float) Math.sin(value);
        float lumR = 0.213f;
        float lumG = 0.715f;
        float lumB = 0.072f;
        float[] mat = new float[]
                {
                        lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
                        lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
                        lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
                        0f, 0f, 0f, 1f, 0f,
                        0f, 0f, 0f, 0f, 1f };
        cm.postConcat(new ColorMatrix(mat));
    }

    protected static float cleanValue(float p_val, float p_limit)
    {
        return Math.min(p_limit, Math.max(-p_limit, p_val));
    }
}
