package com.ljy.htmleditor.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.AlignmentSpan.Standard;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.ljy.htmleditor.R;
import com.ljy.htmleditor.widget.ColorPickerDialog;
import com.ljy.htmleditor.widget.ColorPickerDialog.OnColorChangedListener;
import com.ljy.htmleditor.widget.html.HtmlEditText;
import com.ljy.htmleditor.widget.html.HtmlEditText.OnSelectionChangedListener;
import com.ljy.htmleditor.widget.html.OlBulletSpan;

public class MainActivity extends Activity
{
	private HtmlEditText mEditNoteContent;
	private Button mBtnLeft;
	private Button mBtnLeftChecked;
	private Button mBtnCenter;
	private Button mBtnCenterChecked;
	private Button mBtnRight;
	private Button mBtnRightChecked;
	private Button mBtnUL;
	private Button mBtnOL;
	private Button mBtnBold;
	private Button mBtnItalic;
	private Button mBtnUnderline;
	private Button mBtnMiddleline;
	private Button mBtnForegroundColor;
	private Button mBtnULChecked;
	private Button mBtnOLChecked;
	private Button mBtnBoldChecked;
	private Button mBtnItalicChecked;
	private Button mBtnUnderlineChecked;
	private Button mBtnMiddlelineChecked;

	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{

		}
	};

	protected void onCreate(android.os.Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mEditNoteContent = (HtmlEditText) findViewById(R.id.editNoteContent);
		mBtnLeft = (Button) findViewById(R.id.btnAlignLeft);
		mBtnLeftChecked = (Button) findViewById(R.id.btnAlignLeftChecked);
		mBtnCenter = (Button) findViewById(R.id.btnAlignCenter);
		mBtnCenterChecked = (Button) findViewById(R.id.btnAlignCenterChecked);
		mBtnRight = (Button) findViewById(R.id.btnAlignRight);
		mBtnRightChecked = (Button) findViewById(R.id.btnAlignRightChecked);
		mBtnUL = (Button) findViewById(R.id.btnUL);
		mBtnOL = (Button) findViewById(R.id.btnOL);
		mBtnBold = (Button) findViewById(R.id.btnBold);
		mBtnItalic = (Button) findViewById(R.id.btnItalic);
		mBtnUnderline = (Button) findViewById(R.id.btnUnderline);
		mBtnMiddleline = (Button) findViewById(R.id.btnMiddleline);
		mBtnForegroundColor = (Button) findViewById(R.id.btnForegroundColor);
		mBtnULChecked = (Button) findViewById(R.id.btnULChecked);
		mBtnOLChecked = (Button) findViewById(R.id.btnOLChecked);
		mBtnBoldChecked = (Button) findViewById(R.id.btnBoldChecked);
		mBtnItalicChecked = (Button) findViewById(R.id.btnItalicChecked);
		mBtnUnderlineChecked = (Button) findViewById(R.id.btnUnderlineChecked);
		mBtnMiddlelineChecked = (Button) findViewById(R.id.btnMiddlelineChecked);

		mEditNoteContent.setHandler(mHandler);
		mEditNoteContent.setOnSelectionChangedListener(new OnSelectionChangedListener()
		{
			@Override
			public void onChanged(int selStart, int selEnd)
			{
				updateToolBarStatus();
			}
		});

		// 正文输入框失去焦点后重新设置底部按钮状态
		mEditNoteContent.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				// if (!mEditNoteContent.hasFocus())
				// {
				// mBtnLeft.setVisibility(View.VISIBLE);
				// mBtnCenter.setVisibility(View.VISIBLE);
				// mBtnRight.setVisibility(View.VISIBLE);
				// mBtnUL.setVisibility(View.VISIBLE);
				// mBtnOL.setVisibility(View.VISIBLE);
				// mBtnBold.setVisibility(View.VISIBLE);
				// mBtnItalic.setVisibility(View.VISIBLE);
				// mBtnUnderline.setVisibility(View.VISIBLE);
				// mBtnMiddleline.setVisibility(View.VISIBLE);
				//
				// mBtnLeftChecked.setVisibility(View.GONE);
				// mBtnCenterChecked.setVisibility(View.GONE);
				// mBtnRightChecked.setVisibility(View.GONE);
				// mBtnULChecked.setVisibility(View.GONE);
				// mBtnOLChecked.setVisibility(View.GONE);
				// mBtnBoldChecked.setVisibility(View.GONE);
				// mBtnItalicChecked.setVisibility(View.GONE);
				// mBtnUnderlineChecked.setVisibility(View.GONE);
				// mBtnMiddlelineChecked.setVisibility(View.GONE);
				// }
				// else
				// {
				// mLayoutOperation.setVisibility(View.VISIBLE);
				// }
			}
		});

		OnClickListener listener = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Editable text = mEditNoteContent.getText();

				final int selectStart = mEditNoteContent.getSelectionStart();
				final int selectEnd = mEditNoteContent.getSelectionEnd();
				// LogUtil.d("selectStart : " + selectStart + "; selectEnd : " +
				// selectEnd);
				switch (v.getId())
				{
				case R.id.btnAlignLeft:
					// 左对齐,图片不允许设置左中右对齐
					if (getSpanSize(selectStart, selectEnd, ImageSpan.class) <= 0)
					{
						setAlignmentSpan(text, selectStart, selectEnd,
								Layout.Alignment.ALIGN_NORMAL);
						mBtnLeft.setVisibility(View.GONE);
						mBtnLeftChecked.setVisibility(View.VISIBLE);

						setIMEStatus();
					}
					break;
				case R.id.btnAlignLeftChecked:
					// 取消左对齐,图片不允许设置左中右对齐
					if (getSpanSize(selectStart, selectEnd, ImageSpan.class) <= 0)
					{
						cancelAlignmentSpan(text, selectStart, selectEnd);
						mBtnLeft.setVisibility(View.VISIBLE);
						mBtnLeftChecked.setVisibility(View.GONE);
					}
					break;
				case R.id.btnAlignCenter:
					// 居中对齐,图片不允许设置左中右对齐
					if (getSpanSize(selectStart, selectEnd, ImageSpan.class) <= 0)
					{
						setAlignmentSpan(text, selectStart, selectEnd,
								Layout.Alignment.ALIGN_CENTER);

						setIMEStatus();
						mBtnCenter.setVisibility(View.GONE);
						mBtnCenterChecked.setVisibility(View.VISIBLE);
					}
					break;
				case R.id.btnAlignCenterChecked:
					// 取消居中对齐
					if (getSpanSize(selectStart, selectEnd, ImageSpan.class) <= 0)
					{
						cancelAlignmentSpan(text, selectStart, selectEnd);
						mBtnCenter.setVisibility(View.VISIBLE);
						mBtnCenterChecked.setVisibility(View.GONE);
					}
					break;
				case R.id.btnAlignRight:
					// 右对齐
					if (getSpanSize(selectStart, selectEnd, ImageSpan.class) <= 0)
					{
						setAlignmentSpan(text, selectStart, selectEnd,
								Layout.Alignment.ALIGN_OPPOSITE);

						setIMEStatus();
						mBtnRight.setVisibility(View.GONE);
						mBtnRightChecked.setVisibility(View.VISIBLE);
					}
					break;
				case R.id.btnAlignRightChecked:
					// 取消右对齐
					if (getSpanSize(selectStart, selectEnd, ImageSpan.class) <= 0)
					{
						cancelAlignmentSpan(text, selectStart, selectEnd);
						mBtnRight.setVisibility(View.VISIBLE);
						mBtnRightChecked.setVisibility(View.GONE);
					}
					break;
				case R.id.btnUL:
					mEditNoteContent.setList(text, selectStart, selectEnd, true);
					mBtnUL.setVisibility(View.GONE);
					mBtnULChecked.setVisibility(View.VISIBLE);
					mBtnOL.setVisibility(View.VISIBLE);
					mBtnOLChecked.setVisibility(View.GONE);
					break;
				case R.id.btnOL:
					mEditNoteContent.setList(text, selectStart, selectEnd, false);
					mBtnOL.setVisibility(View.GONE);
					mBtnOLChecked.setVisibility(View.VISIBLE);
					mBtnUL.setVisibility(View.VISIBLE);
					mBtnULChecked.setVisibility(View.GONE);
					break;
				case R.id.btnULChecked:
					mEditNoteContent.cancelList(text, selectStart, selectEnd);
					mBtnUL.setVisibility(View.VISIBLE);
					mBtnULChecked.setVisibility(View.GONE);
					mBtnOL.setVisibility(View.VISIBLE);
					mBtnOLChecked.setVisibility(View.GONE);
					break;
				case R.id.btnOLChecked:
					mEditNoteContent.cancelList(text, selectStart, selectEnd);
					mBtnOL.setVisibility(View.VISIBLE);
					mBtnOLChecked.setVisibility(View.GONE);
					mBtnUL.setVisibility(View.VISIBLE);
					mBtnULChecked.setVisibility(View.GONE);
					break;
				case R.id.btnBold:
					// 粗体
					text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), selectStart,
							selectEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
					mBtnBold.setVisibility(View.GONE);
					mBtnBoldChecked.setVisibility(View.VISIBLE);
					break;
				case R.id.btnItalic:
					// 斜体
					text.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), selectStart,
							selectEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
					mBtnItalic.setVisibility(View.GONE);
					mBtnItalicChecked.setVisibility(View.VISIBLE);
					break;
				case R.id.btnUnderline:
					// 设置下划线
					text.setSpan(new UnderlineSpan(), selectStart, selectEnd,
							Spanned.SPAN_INCLUSIVE_INCLUSIVE);
					mBtnUnderline.setVisibility(View.GONE);
					mBtnUnderlineChecked.setVisibility(View.VISIBLE);
					break;
				case R.id.btnMiddleline:
					// 删除线
					text.setSpan(new StrikethroughSpan(), selectStart, selectEnd,
							Spanned.SPAN_INCLUSIVE_INCLUSIVE);
					mBtnMiddleline.setVisibility(View.GONE);
					mBtnMiddlelineChecked.setVisibility(View.VISIBLE);
					break;
				case R.id.btnForegroundColor:
					final Editable finalText = text;
					new ColorPickerDialog(MainActivity.this, new OnColorChangedListener()
					{
						@Override
						public void colorChanged(int color)
						{
							ForegroundColorSpan[] foregroundColorSpans = finalText.getSpans(
									selectStart, selectEnd, ForegroundColorSpan.class);
							if (foregroundColorSpans != null && foregroundColorSpans.length > 0)
							{
								for (ForegroundColorSpan foregroundColorSpan : foregroundColorSpans)
								{
									mEditNoteContent.cancelSpan(finalText, foregroundColorSpan,
											selectStart, selectEnd);
								}
							}

							finalText.setSpan(new ForegroundColorSpan(color), selectStart,
									selectEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
						}
					}, Color.BLACK).show();
					break;
				case R.id.btnBoldChecked:
					StyleSpan[] boldSpans = text.getSpans(selectStart, selectEnd, StyleSpan.class);
					if (selectStart == selectEnd)
					{
						for (StyleSpan styleSpan : boldSpans)
						{
							if (styleSpan.getStyle() == android.graphics.Typeface.BOLD)
							{
								text.setSpan(styleSpan, text.getSpanStart(styleSpan),
										text.getSpanEnd(styleSpan),
										Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
							}
						}
					}
					else
					{
						for (StyleSpan styleSpan : boldSpans)
						{
							if (styleSpan.getStyle() == android.graphics.Typeface.BOLD)
							{
								mEditNoteContent
										.cancelSpan(text, styleSpan, selectStart, selectEnd);
							}
						}
					}
					mBtnBold.setVisibility(View.VISIBLE);
					mBtnBoldChecked.setVisibility(View.GONE);
					break;
				case R.id.btnItalicChecked:
					StyleSpan[] italicSpans = text
							.getSpans(selectStart, selectEnd, StyleSpan.class);
					if (selectStart == selectEnd)
					{
						for (StyleSpan styleSpan : italicSpans)
						{
							if (styleSpan.getStyle() == android.graphics.Typeface.ITALIC)
							{
								text.setSpan(styleSpan, text.getSpanStart(styleSpan),
										text.getSpanEnd(styleSpan),
										Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
							}
						}
					}
					else
					{
						for (StyleSpan styleSpan : italicSpans)
						{
							if (styleSpan.getStyle() == android.graphics.Typeface.ITALIC)
							{
								mEditNoteContent
										.cancelSpan(text, styleSpan, selectStart, selectEnd);
							}
						}
					}
					mBtnItalic.setVisibility(View.VISIBLE);
					mBtnItalicChecked.setVisibility(View.GONE);
					break;
				case R.id.btnUnderlineChecked:
					UnderlineSpan[] underlineSpans = text.getSpans(selectStart, selectEnd,
							UnderlineSpan.class);
					if (selectStart == selectEnd)
					{
						for (UnderlineSpan styleSpan : underlineSpans)
						{
							text.setSpan(styleSpan, text.getSpanStart(styleSpan),
									text.getSpanEnd(styleSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
					}
					else
					{
						for (UnderlineSpan styleSpan : underlineSpans)
						{
							mEditNoteContent.cancelSpan(text, styleSpan, selectStart, selectEnd);
						}
					}
					mBtnUnderline.setVisibility(View.VISIBLE);
					mBtnUnderlineChecked.setVisibility(View.GONE);
					break;
				case R.id.btnMiddlelineChecked:
					StrikethroughSpan[] strikethroughSpans = text.getSpans(selectStart, selectEnd,
							StrikethroughSpan.class);
					if (selectStart == selectEnd)
					{
						for (StrikethroughSpan styleSpan : strikethroughSpans)
						{
							text.setSpan(styleSpan, text.getSpanStart(styleSpan),
									text.getSpanEnd(styleSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
					}
					else
					{
						for (StrikethroughSpan styleSpan : strikethroughSpans)
						{
							mEditNoteContent.cancelSpan(text, styleSpan, selectStart, selectEnd);
						}
					}
					mBtnMiddleline.setVisibility(View.VISIBLE);
					mBtnMiddlelineChecked.setVisibility(View.GONE);
					break;
				}
			}
		};

		mBtnLeft.setOnClickListener(listener);
		mBtnLeftChecked.setOnClickListener(listener);
		mBtnCenter.setOnClickListener(listener);
		mBtnCenterChecked.setOnClickListener(listener);
		mBtnRight.setOnClickListener(listener);
		mBtnRightChecked.setOnClickListener(listener);
		mBtnUL.setOnClickListener(listener);
		mBtnOL.setOnClickListener(listener);
		mBtnBold.setOnClickListener(listener);
		mBtnItalic.setOnClickListener(listener);
		mBtnUnderline.setOnClickListener(listener);
		mBtnMiddleline.setOnClickListener(listener);
		mBtnForegroundColor.setOnClickListener(listener);
		mBtnULChecked.setOnClickListener(listener);
		mBtnOLChecked.setOnClickListener(listener);
		mBtnBoldChecked.setOnClickListener(listener);
		mBtnItalicChecked.setOnClickListener(listener);
		mBtnUnderlineChecked.setOnClickListener(listener);
		mBtnMiddlelineChecked.setOnClickListener(listener);
	};

	/**
	 * 根据当前光标所处的位置设置底部工具栏按钮的状态
	 * */
	private void updateToolBarStatus()
	{
		mBtnLeft.setVisibility(View.VISIBLE);
		mBtnCenter.setVisibility(View.VISIBLE);
		mBtnRight.setVisibility(View.VISIBLE);
		mBtnUL.setVisibility(View.VISIBLE);
		mBtnOL.setVisibility(View.VISIBLE);
		mBtnBold.setVisibility(View.VISIBLE);
		mBtnItalic.setVisibility(View.VISIBLE);
		mBtnUnderline.setVisibility(View.VISIBLE);
		mBtnMiddleline.setVisibility(View.VISIBLE);

		mBtnLeftChecked.setVisibility(View.GONE);
		mBtnCenterChecked.setVisibility(View.GONE);
		mBtnRightChecked.setVisibility(View.GONE);
		mBtnULChecked.setVisibility(View.GONE);
		mBtnOLChecked.setVisibility(View.GONE);
		mBtnBoldChecked.setVisibility(View.GONE);
		mBtnItalicChecked.setVisibility(View.GONE);
		mBtnUnderlineChecked.setVisibility(View.GONE);
		mBtnMiddlelineChecked.setVisibility(View.GONE);

		if (mEditNoteContent.hasFocus())
		{
			Editable text = mEditNoteContent.getText();
			int selStart = mEditNoteContent.getSelectionStart();
			int selEnd = mEditNoteContent.getSelectionEnd();

			Object[] spans = text.getSpans(selStart, selEnd, Object.class);
			for (Object span : spans)
			{
				if (isInSpan(text, selStart, selEnd, span))
				{
					if (span instanceof StyleSpan)
					{
						StyleSpan styleSpan = (StyleSpan) span;
						if (styleSpan.getStyle() == android.graphics.Typeface.BOLD)
						{
							mBtnBold.setVisibility(View.GONE);
							mBtnBoldChecked.setVisibility(View.VISIBLE);
						}

						if (styleSpan.getStyle() == android.graphics.Typeface.ITALIC)
						{
							mBtnItalic.setVisibility(View.GONE);
							mBtnItalicChecked.setVisibility(View.VISIBLE);
						}
					}
					else if (span instanceof UnderlineSpan)
					{
						mBtnUnderline.setVisibility(View.GONE);
						mBtnUnderlineChecked.setVisibility(View.VISIBLE);
					}
					else if (span instanceof StrikethroughSpan)
					{
						mBtnMiddleline.setVisibility(View.GONE);
						mBtnMiddlelineChecked.setVisibility(View.VISIBLE);
					}
					else if (span instanceof BulletSpan)
					{
						mBtnUL.setVisibility(View.GONE);
						mBtnULChecked.setVisibility(View.VISIBLE);
					}
					else if (span instanceof OlBulletSpan)
					{
						mBtnOL.setVisibility(View.GONE);
						mBtnOLChecked.setVisibility(View.VISIBLE);
					}
					else if (span instanceof AlignmentSpan.Standard)
					{
						AlignmentSpan.Standard alignment = (Standard) span;
						if (alignment.getAlignment() == Layout.Alignment.ALIGN_NORMAL)
						{
							// 左对齐
							mBtnLeft.setVisibility(View.GONE);
							mBtnLeftChecked.setVisibility(View.VISIBLE);
						}
						else if (alignment.getAlignment() == Layout.Alignment.ALIGN_CENTER)
						{
							// 居中对齐
							mBtnCenter.setVisibility(View.GONE);
							mBtnCenterChecked.setVisibility(View.VISIBLE);
						}
						else if (alignment.getAlignment() == Layout.Alignment.ALIGN_OPPOSITE)
						{
							// 右对齐
							mBtnRight.setVisibility(View.GONE);
							mBtnRightChecked.setVisibility(View.VISIBLE);
						}
					}
				}
			}
		}
	}

	private void setAlignmentSpan(Editable text, int selectStart, int selectEnd, Alignment align)
	{
		cancelAlignmentSpan(text, selectStart, selectEnd);
		cancelAlignmentButtonStatus();
		int[] pos = getPreAndEndPosOfCursor(text.toString(), selectStart, selectEnd);
		if (pos[0] == pos[1])
		{
			text.insert(pos[0], HtmlEditText.PLACE_HOLDER);
			pos[1]++;
		}
		text.setSpan(new AlignmentSpan.Standard(align), pos[0], pos[1],
				Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
	}

	private void cancelAlignmentSpan(Editable text, int selectStart, int selectEnd)
	{
		AlignmentSpan.Standard[] alignments = text.getSpans(selectStart, selectEnd,
				AlignmentSpan.Standard.class);
		if (alignments != null && alignments.length > 0)
		{
			for (AlignmentSpan.Standard span : alignments)
			{
				int spanStart = text.getSpanStart(span);
				int spanEnd = text.getSpanEnd(span);

				// 找到实际字符串的起始位置
				int strStart = getPreAndEndPosOfCursor(text.toString(), selectStart, selectStart)[0];
				int strEnd = getPreAndEndPosOfCursor(text.toString(), selectEnd, selectEnd)[1];

				// 1、span全部被选择，取消span
				if (strStart <= spanStart && spanEnd <= strEnd)
				{
					text.removeSpan(span);
				}
				// 2、span右部分被选择，调整span范围，去掉右部分
				else if (spanStart <= strStart && strStart <= spanEnd && spanEnd <= strEnd)
				{
					text.setSpan(span, spanStart, strStart, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				}
				// 3、span左部分被选择，调整span范围，去掉左部分
				else if (strStart <= spanStart && spanStart <= strEnd && strEnd <= spanEnd)
				{
					text.setSpan(span, strEnd, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				}
				// 4、span中间被选择，调整span范围，去掉中间
				else if (spanStart <= strStart && strEnd <= spanEnd)
				{
					text.setSpan(span, spanStart, strStart, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

					AlignmentSpan.Standard span2 = new AlignmentSpan.Standard(span.getAlignment());
					text.setSpan(span2, strEnd, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				}
			}
		}
	}

	/**
	 * 获取光标所在的字符串前后回车的位置
	 * */
	private int[] getPreAndEndPosOfCursor(String text, int selStart, int selEnd)
	{
		int[] pos = new int[2];
		// int index = text.lastIndexOf("\n", curPos);
		int index = -1;
		for (int i = selStart - 1; i >= 0; i--)
		{
			if (text.substring(i, i + 1).equals("\n"))
			{
				index = i;
				break;
			}
		}
		if (index <= 0)
		{
			pos[0] = 0;
		}
		else
		{
			pos[0] = index + 1;
		}

		index = text.indexOf("\n", selEnd);
		if (index <= 0)
		{
			pos[1] = text.length();
		}
		else
		{
			pos[1] = index;
		}
		return pos;
	}

	private void cancelAlignmentButtonStatus()
	{
		mBtnLeft.setVisibility(View.VISIBLE);
		mBtnCenter.setVisibility(View.VISIBLE);
		mBtnRight.setVisibility(View.VISIBLE);

		mBtnLeftChecked.setVisibility(View.GONE);
		mBtnCenterChecked.setVisibility(View.GONE);
		mBtnRightChecked.setVisibility(View.GONE);
	}

	/**
	 * 判断给定地址是否在指定span内
	 * 
	 * 主要是判断在边界的情况<br>
	 * span 包含四种模式<br>
	 * SPAN_INCLUSIVE_EXCLUSIVE = 17<br>
	 * SPAN_INCLUSIVE_INCLUSIVE = 18<br>
	 * SPAN_EXCLUSIVE_EXCLUSIVE = 33<br>
	 * SPAN_EXCLUSIVE_INCLUSIVE = 34<br>
	 * 
	 * */
	private boolean isInSpan(Editable text, int start, int end, Object span)
	{
		int spanStart = text.getSpanStart(span);
		int spanEnd = text.getSpanEnd(span);
		if (start == end)
		{
			int pos = start;
			if (spanStart == pos || pos == spanEnd)
			{
				if (span instanceof BulletSpan || span instanceof OlBulletSpan
						|| span instanceof AlignmentSpan.Standard)
				{
					return true;
				}

				int flag = text.getSpanFlags(span);
				// LogUtil.d("isInSpan flag : " + flag + " : " +
				// span.getClass().getSimpleName());
				// return true;
				if (flag == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
				{
					return false;
				}
				else if (flag == Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
				{
					if (pos == spanStart)
					{
						return false;
					}

					return true;
				}
				else if (flag == Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
				{
					if (pos == spanStart)
					{
						return true;
					}

					return false;
				}
				else if (flag == Spanned.SPAN_INCLUSIVE_INCLUSIVE)
				{
					return true;
				}
			}
			else if (spanStart < pos && pos < spanEnd)
			{
				return true;
			}
		}
		else if (start < end)
		{
			if (start > spanEnd)
			{
				// 选择范围在span右侧
				return false;
			}
			else if (end < spanStart)
			{
				// 选择范围在span左侧
				return false;
			}
			else if (spanStart <= start && end <= spanEnd)
			{
				// 选择范围在span内部
				return true;
			}
			else if (spanStart <= start && start < spanEnd && spanEnd < end)
			{
				// 选择范围包含span右部分
				return true;
			}
			else if (spanStart < end && end <= spanEnd && start < spanStart)
			{
				// 选择范围包含span左部分
				return true;
			}
			else if (start < spanStart && spanEnd < end)
			{
				// 选择范围包含span全部
				return true;
			}
			else if (spanEnd == start && spanEnd < end)
			{
				// 选择范围只包含span最右侧部分
				return isInSpan(text, start, start, span);
			}
			else if (spanStart == end && spanStart > start)
			{
				// 选择范围只包含span最左侧部分
				return isInSpan(text, end, end, span);
			}
		}

		return false;
	}

	private void setIMEStatus()
	{
		InputMethodManager imm = (InputMethodManager) this
				.getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
		View view = this.getCurrentFocus();
		if (view != null)
		{
			// 显示软键盘
			imm.showSoftInput(view, 0);
		}
	}

	private <T> int getSpanSize(int start, int end, Class<T> c)
	{
		Object[] spans = mEditNoteContent.getText().getSpans(start, end, c);
		return spans.length;
	}
}
