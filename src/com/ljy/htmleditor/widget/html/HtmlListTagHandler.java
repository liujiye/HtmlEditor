package com.ljy.htmleditor.widget.html;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Spannable;
import android.text.style.BulletSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;

import com.ljy.htmleditor.html.Html.TagHandler;

public class HtmlListTagHandler implements TagHandler
{
	private int mListItemCount = 0;
	private Vector<String> mListParents = new Vector<String>();

	@Override
	public void handleTag(final boolean opening, final String tag, Editable output,
			Attributes attributes, final XMLReader xmlReader)
	{

		if (tag.equals("ul") || tag.equals("ol") || tag.equals("dd"))
		{
			if (opening)
			{
				mListParents.add(tag);
			}
			else
				mListParents.remove(tag);

			mListItemCount = 0;
		}
		else if (tag.equals("li"))
		{
			handleListTag(output, opening);
		}
		else if (tag.equalsIgnoreCase("code"))
		{
			if (opening)
			{
				output.setSpan(new TypefaceSpan("monospace"), output.length(), output.length(),
						Spannable.SPAN_MARK_MARK);
			}
			else
			{
				Log.d("COde Tag", "Code tag encountered");
				Object obj = getLast(output, TypefaceSpan.class);
				int where = output.getSpanStart(obj);

				output.setSpan(new TypefaceSpan("monospace"), where, output.length(), 0);
			}
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

	private void handleListTag(Editable output, boolean opening)
	{
		String text = output.toString();
		if (output.length() > 0 && output.charAt(output.length() - 1) != '\n')
		{
			output.append("\n");
		}

		if (opening)
		{
			if (mListParents.lastElement().equals("ul"))
			{
				output.setSpan(new BulletSpan(HtmlEditText.sGapWidth), output.length(),
						output.length(), Spannable.SPAN_MARK_MARK);
			}
			else if (mListParents.lastElement().equals("ol"))
			{
				output.setSpan(new OlBulletSpan(0), output.length(), output.length(),
						Spannable.SPAN_MARK_MARK);
			}
		}
		else
		{
			if (mListParents.lastElement().equals("ul"))
			{
				Object obj = getLast(output, BulletSpan.class);
				int start = output.getSpanStart(obj);
				output.removeSpan(obj);
				output.setSpan(new BulletSpan(HtmlEditText.sGapWidth), start, output.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				// output.setSpan(new BulletSpan(15 * mListParents.size()),
				// start, output.length(), 0);
			}
			else if (mListParents.lastElement().equals("ol"))
			{
				mListItemCount++;

				Object obj = getLast(output, OlBulletSpan.class);
				int start = output.getSpanStart(obj);
				output.removeSpan(obj);
				output.setSpan(new OlBulletSpan(mListItemCount), start, output.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				Log.d("tagHandler", "start : " + start + ";end : " + output.length());
			}
		}
	}
}
