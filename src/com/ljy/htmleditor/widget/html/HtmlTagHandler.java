package com.ljy.htmleditor.widget.html;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

import com.ljy.htmleditor.html.Html.TagHandler;

public class HtmlTagHandler implements TagHandler
{
	HtmlListTagHandler mHtmlListTagHandler = new HtmlListTagHandler();

	@Override
	public void handleTag(boolean opening, String tag, Editable output, Attributes attributes,
			XMLReader xmlReader)
	{
		// 如果fromHtml中有发现无法识别的标签就会吐到taghandler接口上，String tag就是这些标签。
		Log.d("", "handleTag--->" + tag);
		if (tag.equalsIgnoreCase("strike") || tag.equals("s"))
		{
			// 中划线
			processStrike(opening, output);
		}
		else if (tag.equalsIgnoreCase("size6"))
		{
			processSize(opening, output);
		}
		else if (tag.equalsIgnoreCase("li") || tag.equalsIgnoreCase("ul")
				|| tag.equalsIgnoreCase("ol"))
		{
			// processUlli(opening, output);
			mHtmlListTagHandler.handleTag(opening, tag, output, attributes, xmlReader);
		}
		else if (tag.equalsIgnoreCase("span"))
		{
			processSpan(opening, output, attributes);
		}
	}

	private void processStrike(boolean opening, Editable output)
	{
		Log.d("", "processStrike--->" + output);
		int len = output.length();
		if (opening)
		{
			output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
		}
		else
		{
			Object obj = getLast(output, StrikethroughSpan.class);
			int where = output.getSpanStart(obj);

			output.removeSpan(obj);

			if (where != len)
			{
				output.setSpan(new StrikethroughSpan(), where, len,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	private void processSize(boolean opening, Editable output)
	{
		Log.d("", "processSize--->" + output);
		int len = output.length();
		if (opening)
		{
			output.setSpan(new AbsoluteSizeSpan(40), len, len, Spannable.SPAN_MARK_MARK);
		}
		else
		{
			Object obj = getLast(output, AbsoluteSizeSpan.class);
			int where = output.getSpanStart(obj);

			output.removeSpan(obj);

			if (where != len)
			{
				output.setSpan(new AbsoluteSizeSpan(40), where, len,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	// mFontSize = "",
	String mTextDecoration = "", mColor = "";

	private void processSpan(boolean opening, Editable output, Attributes attributes)
	{
		// System.out.println("processSpan--->" + output);
		int len = output.length();
		if (opening)
		{
			mTextDecoration = "";
			// mFontSize = "";
			mColor = "";
			// output.setSpan(new AbsoluteSizeSpan(40), len, len,
			// Spannable.SPAN_MARK_MARK);
			String debug = "";
			if (attributes != null && attributes.getLength() > 1)
			{
				String before = attributes.getValue(0);
				debug += before;
				for (int i = 0; i < attributes.getLength(); i++)
				{
					String value = attributes.getValue(i).trim();
					debug += value;

					if (value.equalsIgnoreCase("text-decoration:")
							|| value.equalsIgnoreCase("text-decoration:_")
							// || value.equalsIgnoreCase("font-size:_")
							// || value.equalsIgnoreCase("font-size:")
							|| value.equalsIgnoreCase("color:_")
							|| value.equalsIgnoreCase("color:"))
					{
						before = value;
						continue;
					}

					if (before.equalsIgnoreCase("text-decoration:")
							|| before.equalsIgnoreCase("text-decoration:_"))
					{
						mTextDecoration = value;
						before = "";
					}
					// else if (before.equalsIgnoreCase("font-size:_")
					// || before.equalsIgnoreCase("font-size:"))
					// {
					// mFontSize = value;
					// before = "";
					// }
					else if (before.equalsIgnoreCase("color:_")
							|| before.equalsIgnoreCase("color:"))
					{
						mColor += value;
					}
				}

				Log.d("processSpan debug", debug);
			}
			else if (attributes != null && attributes.getLength() == 1)
			{
				String style = attributes.getValue("style");
				if (style != null && style.length() > 0)
				{
					style = style.trim().toLowerCase();

					String textDecorationReg = "text-decoration *: *([\\w\\W]+?)[; ]";
					String colorReg = "color *: *(rgb\\( *\\d{1,3} *, *\\d{1,3} *, *\\d{1,3} *\\)+?)|(#\\d+);";
					// String fontSizeReg = "font-size *: *([\\w\\W]+?);";

					// 表达式对象
					Pattern p = Pattern.compile(textDecorationReg);

					// 创建 Matcher 对象
					Matcher m = p.matcher(style);

					// 是否找到匹配
					if (m.find())
					{
						mTextDecoration = m.group(1);
					}

					p = Pattern.compile(colorReg);
					m = p.matcher(style);
					if (m.find())
					{
						mColor = m.group(1);
					}

					// p = Pattern.compile(fontSizeReg);
					// m = p.matcher(style);
					// if (m.find())
					// {
					// mFontSize = m.group(1);
					// }
				}
			}

			if (mTextDecoration.equalsIgnoreCase("line-through"))
			{
				output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
			}
			else if (mTextDecoration.equalsIgnoreCase("underline"))
			{
				output.setSpan(new UnderlineSpan(), len, len, Spannable.SPAN_MARK_MARK);
			}

			// if (mFontSize.length() > 0)
			// {
			// mFontSize = mFontSize.toLowerCase();
			// if (mFontSize.endsWith("px"))
			// {
			// int size = Integer.valueOf(mFontSize.substring(1,
			// mFontSize.length() - 2));
			// output.setSpan(new AbsoluteSizeSpan(size), len, len,
			// Spannable.SPAN_MARK_MARK);
			// }
			// else
			// {
			// int size = Integer.valueOf(mFontSize.substring(1,
			// mFontSize.length()));
			// output.setSpan(new RelativeSizeSpan(size / 100.0f), len, len,
			// Spannable.SPAN_MARK_MARK);
			// }
			// }

			if (mColor.length() > 0)
			{
				output.setSpan(new ForegroundColorSpan(0), len, len, Spannable.SPAN_MARK_MARK);
			}
		}
		else
		{
			if (mTextDecoration.equalsIgnoreCase("line-through"))
			{
				StrikethroughSpan obj = (StrikethroughSpan) getLast(output, StrikethroughSpan.class);
				int where = output.getSpanStart(obj);

				output.removeSpan(obj);

				if (where != len)
				{
					output.setSpan(new StrikethroughSpan(), where, len,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			else if (mTextDecoration.equalsIgnoreCase("underline"))
			{
				UnderlineSpan obj = (UnderlineSpan) getLast(output, UnderlineSpan.class);
				int where = output.getSpanStart(obj);

				output.removeSpan(obj);

				if (where != len)
				{
					output.setSpan(new UnderlineSpan(), where, len,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			mTextDecoration = "";

			// if (mFontSize.length() > 0)
			// {
			// mFontSize = mFontSize.toLowerCase();
			//
			// if (mFontSize.endsWith("px"))
			// {
			// AbsoluteSizeSpan obj = (AbsoluteSizeSpan) getLast(output,
			// AbsoluteSizeSpan.class);
			// int where = output.getSpanStart(obj);
			//
			// output.removeSpan(obj);
			//
			// if (where != len)
			// {
			// output.setSpan(new AbsoluteSizeSpan(obj.getSize()), where, len,
			// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			// }
			// }
			// else
			// {
			// RelativeSizeSpan obj = (RelativeSizeSpan) getLast(output,
			// RelativeSizeSpan.class);
			// int where = output.getSpanStart(obj);
			//
			// output.removeSpan(obj);
			//
			// if (where != len)
			// {
			// output.setSpan(new RelativeSizeSpan(obj.getSizeChange()), where,
			// len,
			// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			// }
			// }
			// }
			// mFontSize = "";

			if (mColor.length() > 0)
			{
				ForegroundColorSpan obj = (ForegroundColorSpan) getLast(output,
						ForegroundColorSpan.class);
				int where = output.getSpanStart(obj);

				output.removeSpan(obj);

				if (where != len)
				{
					int red = 0, green = 0, blue = 0;
					Matcher m = Pattern.compile(
							"rgb\\(( *\\d{1,3} *), ( *\\d{1,3} *),( *\\d{1,3} *)\\)").matcher(
							mColor);
					if (m.matches())
					{
						red = Integer.valueOf(m.group(1).trim());
						green = Integer.valueOf(m.group(2).trim());
						blue = Integer.valueOf(m.group(3).trim());
					}
					else if ((m = Pattern.compile("#\\d+").matcher(mColor)).matches())
					{
						int color = Color.parseColor(mColor);
						red = Color.red(color);
						green = Color.green(color);
						blue = Color.blue(color);
					}
					else
					{
						int index = mColor.indexOf("rgb");
						if (index != -1)
						{
							String color = mColor.substring(index + 3);
							String[] colors = color.split("_");
							List<String> colorList = new ArrayList<String>();
							for (String strColor : colors)
							{
								if (strColor.length() > 0)
								{
									colorList.add(strColor);
								}
							}
							red = (colorList.size() >= 1 ? Integer.valueOf(colorList.get(0)) : 0);
							green = (colorList.size() >= 2 ? Integer.valueOf(colorList.get(1)) : 0);
							blue = (colorList.size() >= 3 ? Integer.valueOf(colorList.get(2)) : 0);
						}
					}

					output.setSpan(new ForegroundColorSpan(Color.rgb(red, green, blue)), where,
							len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			mColor = "";
		}
	}

	private Object getLast(Editable text, Class kind)
	{
		Object[] objs = text.getSpans(0, text.length(), kind);

		if (objs.length == 0)
		{
			return null;
		}
		else
		{
			for (int i = objs.length; i > 0; i--)
			{
				if (text.getSpanFlags(objs[i - 1]) == Spannable.SPAN_MARK_MARK)
				{
					return objs[i - 1];
				}
			}
			return null;
		}
	}
}
