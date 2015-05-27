package com.ljy.htmleditor.widget.html;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

/**
 * 有序的列表span，类似
 * <ol>
 * <li>item 1</li>
 * <li>item 2</li>
 * </ol>
 * */
public class OlBulletSpan implements LeadingMarginSpan, ParcelableSpan
{
	private int mIndex = -1;
	private final int mGapWidth;
	private final boolean mWantColor;
	private final int mColor;

	private static final int BULLET_RADIUS = 3;
	private static Path sBulletPath = null;
	public static final int STANDARD_GAP_WIDTH = 45;

	/**
	 * 有序的列表span，类似
	 * <ol>
	 * <li>item 1</li>
	 * <li>item 2</li>
	 * </ol>
	 * 
	 * @param index
	 *            : 序号
	 * */
	public OlBulletSpan(int index)
	{
		mIndex = index;
		mGapWidth = STANDARD_GAP_WIDTH;
		mWantColor = false;
		mColor = 0;
	}

	public OlBulletSpan(int index, int gapWidth)
	{
		mIndex = index;
		mGapWidth = gapWidth;
		mWantColor = false;
		mColor = 0;
	}

	public OlBulletSpan(int index, int gapWidth, int color)
	{
		mIndex = index;
		mGapWidth = gapWidth;
		mWantColor = true;
		mColor = color;
	}

	public OlBulletSpan(Parcel src)
	{
		mIndex = src.readInt();
		mGapWidth = src.readInt();
		mWantColor = src.readInt() != 0;
		mColor = src.readInt();
	}

	public int getIndex()
	{
		return mIndex;
	}

	public void setIndex(int index)
	{
		mIndex = index;
	}

	@Override
	public int getSpanTypeId()
	{
		return 0;
	}

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeInt(mIndex);
		dest.writeInt(mGapWidth);
		dest.writeInt(mWantColor ? 1 : 0);
		dest.writeInt(mColor);
	}

	public int getLeadingMargin(boolean first)
	{
		return mGapWidth;
	}

	public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline,
			int bottom, CharSequence text, int start, int end, boolean first, Layout l)
	{
		if (((Spanned) text).getSpanStart(this) == start)
		{
			Paint.Style style = p.getStyle();
			int oldcolor = 0;

			if (mWantColor)
			{
				oldcolor = p.getColor();
				p.setColor(mColor);
			}

			p.setStyle(Paint.Style.FILL);
			// p.setTextAlign(Align.RIGHT);// 右对齐

			c.drawText(String.valueOf(mIndex) + ".", x + dir * BULLET_RADIUS, baseline, p);

			if (mWantColor)
			{
				p.setColor(oldcolor);
			}

			p.setStyle(style);
		}
	}
}
