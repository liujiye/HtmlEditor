package com.ljy.htmleditor.widget.html;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import com.ljy.htmleditor.html.Html;

public class HtmlEditText extends EditText
{
	public final static int sGapWidth = 20;
	public final static int TEXT_CHANGED = 1000;
	private Handler mHandler;

	public interface OnSelectionChangedListener
	{
		public void onChanged(int selStart, int selEnd);
	}

	private OnSelectionChangedListener mOnSelectionChangedListener;

	/** 新建一个空的OL或者Ul需要的占位符，值为ASCII 0 */
	public static final String PLACE_HOLDER = String.valueOf((char) 0);

	public HtmlEditText(Context context)
	{
		super(context);
		init();
	}

	public HtmlEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public HtmlEditText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd)
	{
		super.onSelectionChanged(selStart, selEnd);
		if (mOnSelectionChangedListener != null)
		{
			mOnSelectionChangedListener.onChanged(selStart, selEnd);
		}
	}

	public void setOnSelectionChangedListener(OnSelectionChangedListener listener)
	{
		mOnSelectionChangedListener = listener;
	}

	public void setHandler(Handler handler)
	{
		mHandler = handler;
	}

	private void init()
	{
		addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// 先把当前光标所处位置的span设置为SPAN_INCLUSIVE_INCLUSIVE，方便输入
				// 等到输入完成后再设置回SPAN_EXCLUSIVE_EXCLUSIVE
				Editable text = (Editable) s;
				setSpanInclude(text, start);
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				Editable text = (Editable) s;

				Log.d("start", "start : " + start + "; before : " + before + "; count : " + count);
				int end = start + count - before;
				if (start <= end)
				{
					String strInput = text.subSequence(start, end).toString();
					if (strInput.equals(PLACE_HOLDER))
					{
						return;
					}
				}

				removeUselessSpan(text);

				processInput(text, start, before, count);

				// 重新设置ol序号
				// resetOrderNum(text);
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				// resetSpanFlags(s);
				resetOlSpanRange(s);
				resetOrderNum(s);

				if (mHandler != null)
				{
					mHandler.sendEmptyMessageDelayed(TEXT_CHANGED, 10);
				}
			}
		});
	}

	private void processInput(Editable text, int start, int before, int count)
	{
		int end = start + count - before;
		if (end > start)
		{
			String strInput = text.subSequence(start, end).toString();
			if (strInput.equals("\n"))
			{
				resetAlignmentSpanFlag();
				processInputEnter(text, start, before, count);
			}
		}
	}

	/**
	 * 处理输入回车的情况<br>
	 * 
	 * <ol>
	 * <li>在list中间输入回车，则将原来的一条list分成两条list，回车符包含在原来的list项中</li>
	 * <li>在list尾部输入回车</li>
	 * <ul>
	 * <li>前一个列表项如果不为空，则新建一个列表项，回车符包含在原来的list项中</li>
	 * <li>前一个列表项如果为空，则删除前一个列表项</li>
	 * </ul>
	 * <li>在list前方输入回车，在当前list项前方添加一条新的list项</li>
	 * </ol>
	 * */
	private void processInputEnter(Editable text, int start, int before, int count)
	{
		int end = start + count - before;
		LeadingMarginSpan[] spans = text.getSpans(start, start, LeadingMarginSpan.class);
		if (spans.length == 1)
		{
			LeadingMarginSpan span = spans[0];
			int spanStart = text.getSpanStart(span);
			int spanEnd = text.getSpanEnd(span);

			// 前一个list项为空，则删除该list项，并删除该list项的占位符
			if (isNullList(text, span))
			{
				text.removeSpan(span);
				text.delete(spanStart, spanStart + 1);
				return;
			}

			// 输入的位置在span内部
			if (spanStart <= start && start <= spanEnd)
			{
				text.setSpan(span, spanStart, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

				// 如果start跟spanend相等，flags为SPAN_EXCLUSIVE_EXCLUSIVE时调用getspans获取不到该span
				int newEnd = (end > spanEnd ? end : spanEnd);
				if (newEnd == end)
				{
					text.insert(end, PLACE_HOLDER);
					newEnd++;
				}

				if (span instanceof BulletSpan)
				{
					text.setSpan(new BulletSpan(sGapWidth), end, newEnd,
							Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				}
				else if (span instanceof OlBulletSpan)
				{
					text.setSpan(new OlBulletSpan(0), end, newEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
		}
		else
		{
			// 回车的地方应该只能有一个list span ，否则删掉空白的
			for (LeadingMarginSpan span : spans)
			{
				int spanStart = text.getSpanStart(span);
				// int spanEnd = text.getSpanEnd(span);

				if (spanStart == start)
				{
					text.removeSpan(span);
				}
			}
		}
	}

	/**
	 * 判断list项是否为空<br>
	 * list项只包含"\n"、""、PLACE_HOLDER + "\n"、PLACE_HOLDER则为空
	 * */
	private boolean isNullList(Editable text, Object span)
	{
		int spanStart = text.getSpanStart(span);
		int spanEnd = text.getSpanEnd(span);
		String str = text.subSequence(spanStart, spanEnd).toString();
		if (str.equals("\n") || str.equals("") || str.equals(PLACE_HOLDER + "\n")
				|| str.equals(PLACE_HOLDER))
		{
			return true;
		}

		return false;
	}

	private void setSpanInclude(Editable text, int pos)
	{
		// CharacterStyle[] spans = text.getSpans(pos, pos,
		// CharacterStyle.class);
		// for (CharacterStyle span : spans)
		// {
		// text.setSpan(span, text.getSpanStart(span), text.getSpanEnd(span),
		// Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		// }

		LeadingMarginSpan[] leadSpans = text.getSpans(pos, pos, LeadingMarginSpan.class);
		for (LeadingMarginSpan span : leadSpans)
		{
			text.setSpan(span, text.getSpanStart(span), text.getSpanEnd(span),
					Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		}
	}

	private void resetSpanFlags(Editable text)
	{
		CharacterStyle[] spans = text.getSpans(0, text.length(), CharacterStyle.class);
		for (CharacterStyle span : spans)
		{
			text.setSpan(span, text.getSpanStart(span), text.getSpanEnd(span),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		LeadingMarginSpan[] leadSpans = text.getSpans(0, text.length(), LeadingMarginSpan.class);
		for (LeadingMarginSpan span : leadSpans)
		{
			text.setSpan(span, text.getSpanStart(span), text.getSpanEnd(span),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	private List<Object> getAllSpans(Editable text)
	{
		Object[] spans = text.getSpans(0, text.length(), Object.class);
		List<Object> spanList = new ArrayList<Object>();
		for (Object span : spans)
		{
			String packageName = span.getClass().getPackage().getName();
			if (packageName.startsWith("android.text.style")
					|| packageName.startsWith(this.getClass().getPackage().getName()))
			{
				spanList.add(span);
			}
		}

		return spanList;
	}

	/**
	 * 返回排序后的列表，只有BulletSpan和OlBulletSpan
	 * */
	private List<Object> getSortedSpans(Editable text)
	{
		// removeUselessSpan(text);

		Object[] spans = text.getSpans(0, text.length(), Object.class);
		List<Object> spanList = new ArrayList<Object>();

		for (Object span : spans)
		{
			if (span instanceof BulletSpan || span instanceof OlBulletSpan)
			{
				int spanStart = text.getSpanStart(span);
				// int spanEnd = text.getSpanEnd(span);
				int i = 0;
				for (; i < spanList.size(); i++)
				{
					Object temp = spanList.get(i);
					int start = text.getSpanStart(temp);
					// int end = text.getSpanEnd(temp);

					if (spanStart <= start)
					{
						spanList.add(i, span);
						break;
					}
				}

				if (i == spanList.size())
				{
					spanList.add(span);
				}
			}
		}

		// debug
		// for (Object span : spanList)
		// {
		// int span_start = text.getSpanStart(span);
		// int span_end = text.getSpanEnd(span);
		// Log.d("sorted", "span_start : " + span_start + "; span_end : " +
		// span_end);
		// }

		return spanList;
	}

	/**
	 * 遍历全文，获得指定位置的span
	 * */
	public List<Object> getSpannables(Editable text, int pos)
	{
		List<Object> spanList = getAllSpans(text);
		List<Object> spans = new ArrayList<Object>();

		for (Object span : spanList)
		{
			int start = text.getSpanStart(span);
			int end = text.getSpanEnd(span);

			if (pos >= start && pos <= end)
			{
				spans.add(span);
			}
		}

		return spans;
	}

	private void resetOlSpanRange(Editable text)
	{
		// LeadingMarginSpan[] spans = text.getSpans(0, text.length(),
		// LeadingMarginSpan.class);
		List<Object> spans = getSortedSpans(text);
		for (int i = 0; i < spans.size(); i++)
		{
			Object span1 = spans.get(i);
			int start1 = text.getSpanStart(span1);
			int end1 = text.getSpanEnd(span1);

			for (int j = i + 1; j < spans.size(); j++)
			{
				Object span2 = spans.get(j);
				int start2 = text.getSpanStart(span2);
				int end2 = text.getSpanEnd(span1);

				if (start1 <= start2 && start2 <= end1 && start1 <= end2 && end2 <= end1)
				{
					text.setSpan(span1, start1, start2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
	}

	/**
	 * 重新设置所有ol的序号
	 * */
	private void resetOrderNum(Editable text)
	{
		List<Object> sortList = getSortedSpans(text);
		int beforeEnd = 0;
		int index = 1;
		for (Object span : sortList)
		{
			if (span instanceof OlBulletSpan)
			{
				int span_start = text.getSpanStart(span);
				int span_end = text.getSpanEnd(span);
				if (beforeEnd != span_start)
				{
					index = 1;
				}

				((OlBulletSpan) span).setIndex(index++);
				beforeEnd = span_end;
				Log.d("end", "span_start : " + span_start + "; span_end : " + span_end);
			}
		}
	}

	/**
	 * 设置有序和无序列表
	 * 
	 * @param isUL
	 *            : true : 设置无序列表，false : 设置有序列表
	 * */
	public void setList(Editable text, int selectStart, int selectEnd, boolean isUL)
	{
		int minStart = selectStart, maxEnd = selectEnd;

		// 找到选择的该行的起始位置
		while (minStart > 0 && text.charAt(minStart - 1) != '\n')
		{
			minStart--;
		}

		LeadingMarginSpan[] spans = text.getSpans(selectStart, selectEnd, LeadingMarginSpan.class);

		// 重新确定操作的范围
		for (LeadingMarginSpan span : spans)
		{
			int span_start = text.getSpanStart(span);
			int span_end = text.getSpanEnd(span);

			if (minStart > span_start)
			{
				minStart = span_start;
			}

			if (maxEnd < span_end)
			{
				maxEnd = span_end;
			}
		}

		String[] splits = text.subSequence(minStart, maxEnd).toString().split("\n");
		int start = minStart;
		for (String split : splits)
		{
			start = text.toString().indexOf(split, start);
			int end = start + split.length() + 1;
			end = (end > text.length() ? text.length() : end);

			LeadingMarginSpan[] tempSpans = text.getSpans(start, end, LeadingMarginSpan.class);
			for (LeadingMarginSpan span : tempSpans)
			{
				text.removeSpan(span);
			}

			if (start == end)
			{
				text.insert(start, PLACE_HOLDER);
				end++;
			}

			if (isUL)
			{
				text.setSpan(new BulletSpan(sGapWidth), start, end,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			else
			{
				text.setSpan(new OlBulletSpan(0), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

			start = end;
		}

		resetOrderNum(text);
	}

	/**
	 * 取消有序或者无序列表
	 * */
	public void cancelList(Editable text, int selectStart, int selectEnd)
	{
		int minStart = selectStart, maxEnd = selectEnd;

		// 找到选择的该行的起始位置
		while (minStart > 0 && text.charAt(minStart - 1) != '\n')
		{
			minStart--;
		}

		LeadingMarginSpan[] spans = text.getSpans(selectStart, selectEnd, LeadingMarginSpan.class);

		// 重新确定操作的范围
		for (LeadingMarginSpan span : spans)
		{
			int span_start = text.getSpanStart(span);
			int span_end = text.getSpanEnd(span);

			if (minStart > span_start)
			{
				minStart = span_start;
			}

			if (maxEnd < span_end)
			{
				maxEnd = span_end;
			}
		}

		String[] splits = text.subSequence(minStart, maxEnd).toString().split("\n");
		int start = minStart;
		for (String split : splits)
		{
			start = text.toString().indexOf(split, start);
			int end = start + split.length() + 1;
			end = (end > text.length() ? text.length() : end);

			LeadingMarginSpan[] tempSpans = text.getSpans(start, end, LeadingMarginSpan.class);
			for (LeadingMarginSpan span : tempSpans)
			{
				text.removeSpan(span);
			}

			start = end;
		}

		resetOrderNum(text);
	}

	public void cancelSpan(Editable text, Object span, int selectStart, int selectEnd)
	{
		int start = text.getSpanStart(span);
		int end = text.getSpanEnd(span);
		if (selectStart <= start && end <= selectEnd)
		{
			// 选择的范围包含span
			// +----+
			// ------
			// 移除在选择范围内的颜色span
			text.removeSpan(span);
		}
		else if (selectStart <= start && start <= selectEnd && selectEnd <= end)
		{
			// 选择的范围包含原有span的左部分
			// ++----
			// ----
			// 修改原有span的范围
			text.setSpan(span, selectEnd, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		else if (selectEnd >= end && start <= selectStart && selectStart <= end)
		{
			// 选择范围包含原有span的右部分
			// ----
			// ++----
			// 修改原有span的范围
			text.setSpan(span, start, selectStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		else if (start <= selectStart && selectEnd <= end)
		{
			// 选择的范围属于原有span内
			// ----
			// +--+
			text.setSpan(span, start, selectStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			Object newSpan = null;
			if (span instanceof StyleSpan)
			{
				StyleSpan styleSpan = (StyleSpan) span;
				newSpan = new StyleSpan(styleSpan.getStyle());
			}
			else if (span instanceof UnderlineSpan)
			{
				newSpan = new UnderlineSpan();
			}
			else if (span instanceof StrikethroughSpan)
			{
				newSpan = new StrikethroughSpan();
			}
			else if (span instanceof ForegroundColorSpan)
			{
				ForegroundColorSpan foregroundColorSpan = (ForegroundColorSpan) span;
				newSpan = new ForegroundColorSpan(foregroundColorSpan.getForegroundColor());
			}

			if (newSpan != null)
			{
				text.setSpan(newSpan, selectEnd, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	/**
	 * 移除text中无用的span，当spannable的start和end相等的时候就判断为无用的
	 * */
	private void removeUselessSpan(Editable text)
	{
		List<Object> spanList = getAllSpans(text);

		for (Object span : spanList)
		{
			int start = text.getSpanStart(span);
			int end = text.getSpanEnd(span);

			if (start == end)
			{
				Log.d("removeSpan", span.getClass().getSimpleName());
				text.removeSpan(span);
			}
		}
	}

	public void resetAlignmentSpanFlag()
	{
		Editable text = getText();
		AlignmentSpan.Standard[] alignments = text.getSpans(0, text.length(),
				AlignmentSpan.Standard.class);
		for (AlignmentSpan.Standard alignment : alignments)
		{
			int start = text.getSpanStart(alignment);
			int end = text.getSpanEnd(alignment);
			text.setSpan(alignment, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	/**
	 * 去掉输入时插入的占位符
	 * */
	private void removePlaceHolder()
	{
		Editable text = getText();
		String str = text.toString();
		int index = str.lastIndexOf(PLACE_HOLDER);
		while (index != -1)
		{
			text.delete(index, index + 1);

			str = text.toString();
			index = str.lastIndexOf(PLACE_HOLDER);
		}
	}

	public String toHtml()
	{
		removePlaceHolder();
		Editable text = getText();

		String CONST_UL = "!@#";
		String CONST_OL = "$%^";
		int len = text.length();
		Editable tempText = (Editable) text.subSequence(0, len);

		// 获得所有edittext内的所有span
		// Object[] spans = text.getSpans(0, len, Object.class);
		List<Object> spans = getSortedSpans(text);
		List<Ul> uls = new ArrayList<Ul>();
		List<Ol> ols = new ArrayList<HtmlEditText.Ol>();
		for (Object span : spans)
		{
			if (span instanceof BulletSpan)
			{
				int start = text.getSpanStart(span);
				int end = text.getSpanEnd(span);
				addSpantoUlList(uls, (BulletSpan) span, start, end, text);
			}
			else if (span instanceof OlBulletSpan)
			{
				int start = text.getSpanStart(span);
				int end = text.getSpanEnd(span);
				addSpanToOlList(ols, (OlBulletSpan) span, start, end, tempText);
			}
		}

		// 将ul列表和其他正文分离处理
		for (int i = uls.size() - 1; i >= 0; i--)
		{
			Ul ul = uls.get(i);
			int start = ul.getStart(tempText);
			int end = ul.getEnd(tempText);
			Object[] tempSpans = tempText.getSpans(start, end, Object.class);
			tempText = tempText.replace(ul.getStart(tempText), ul.getEnd(tempText), CONST_UL);
			for (Object span : tempSpans)
			{
				tempText.removeSpan(span);
			}
		}

		// 将ol列表和其他正文分离处理
		for (int i = ols.size() - 1; i >= 0; i--)
		{
			Ol ol = ols.get(i);
			int start = ol.getStart(tempText);
			int end = ol.getEnd(tempText);
			Object[] tempSpans = tempText.getSpans(start, end, Object.class);
			tempText = tempText.replace(ol.getStart(tempText), ol.getEnd(tempText), CONST_OL);
			for (Object span : tempSpans)
			{
				tempText.removeSpan(span);
			}
		}

		// 处理除列表外的其他正文
		StringBuilder sbHtml = new StringBuilder();
		int start = 0;
		for (int end = tempText.toString().indexOf("\n", start); end != -1;)
		{
			if (end > start)
			{
				sbHtml.append(processSubEditable(tempText, start, end));
			}
			else if (end == start)
			{
				// 一个p标签后面紧跟着的回车就不需要了
				if (!sbHtml.toString().endsWith("</p>"))
				{
					sbHtml.append("<br>");
				}
			}

			if (start == end)
			{
				start = end + 1;
			}
			else
			{
				start = end;
			}

			end = tempText.toString().indexOf("\n", start);
			if (end == -1)
			{
				sbHtml.append(processSubEditable(tempText, start, tempText.length()));
			}
		}

		if (!tempText.toString().contains("\n"))
		{
			sbHtml.append(processSubEditable(tempText, 0, tempText.length()));
		}

		Log.d("temp html", sbHtml.toString());

		// 处理列表，合并其他的正文
		for (Ul ul : uls)
		{
			String str = ul.toHtml(text);
			// html = html.replaceFirst(CONST, str);
			int index = sbHtml.indexOf(CONST_UL);
			sbHtml.replace(index, index + CONST_UL.length(), str);
		}

		for (Ol ol : ols)
		{
			String str = ol.toHtml(text);
			int index = sbHtml.indexOf(CONST_OL);
			sbHtml.replace(index, index + CONST_OL.length(), str);
		}

		String html = sbHtml.toString();
		Log.d("html", html);
		return html;
	}

	/**
	 * 将文本按照"\n"分隔后调用html.toHtml处理，因为需要处理掉多余的
	 * <p>
	 * 标签
	 * */
	private String processSubEditable(Editable text, int start, int end)
	{
		Editable subEditable = (Editable) text.subSequence(start, end);
		StringBuilder sb = new StringBuilder(Html.toHtml(subEditable).trim());

		if (sb.toString().startsWith("<p>") && sb.toString().endsWith("</p>"))
		{
			int subStart = sb.indexOf("<p>") + "<p>".length();
			int subEnd = sb.lastIndexOf("</p>");

			return sb.substring(subStart, subEnd);
		}

		return sb.toString();
	}

	/**
	 * 插入span
	 * 
	 * @param start
	 *            : span 的起始位置
	 * */
	private void addSpantoUlList(List<Ul> ulList, BulletSpan span, int start, int end, Editable text)
	{
		if (ulList.size() > 0)
		{
			int spanStart = ulList.get(0).getStart(text);
			int spanEnd = ulList.get(ulList.size() - 1).getEnd(text);
			if (end < spanStart)
			{
				Ul ul = new Ul();
				ul.addBulletSpan(span);
				ulList.add(0, ul);
				return;
			}
			else if (start > spanEnd)
			{
				Ul ul = new Ul();
				ul.addBulletSpan(span);
				ulList.add(ul);
				return;
			}

			for (Ul ul : ulList)
			{
				if (ul.addBulletSpan(span, start, end, text))
				{
					return;
				}
			}
		}
		else
		{
			Ul ul = new Ul();
			ul.addBulletSpan(span);
			ulList.add(ul);
		}
	}

	private void addSpanToOlList(List<Ol> ulList, OlBulletSpan span, int start, int end,
			Editable text)
	{
		if (ulList.size() > 0)
		{
			int spanStart = ulList.get(0).getStart(text);
			int spanEnd = ulList.get(ulList.size() - 1).getEnd(text);
			if (end < spanStart)
			{
				Ol ul = new Ol();
				ul.addBulletSpan(span);
				ulList.add(0, ul);
				return;
			}
			else if (start > spanEnd)
			{
				Ol ul = new Ol();
				ul.addBulletSpan(span);
				ulList.add(ul);
				return;
			}

			for (Ol ul : ulList)
			{
				if (ul.addBulletSpan(span, start, end, text))
				{
					return;
				}
			}
		}
		else
		{
			Ol ul = new Ol();
			ul.addBulletSpan(span);
			ulList.add(ul);
		}
	}

	class HtmlList
	{
		public List<LeadingMarginSpan> mSpans = null;

		public HtmlList()
		{
			mSpans = new ArrayList<LeadingMarginSpan>();
		}

		public void addBulletSpan(LeadingMarginSpan span)
		{
			mSpans.add(span);
		}

		public boolean addBulletSpan(LeadingMarginSpan span, int start, int end, Editable text)
		{
			// 要插入的span跟现有的span不相连，没有交集，不插入
			if (mSpans.size() > 0 && (end < getStart(text) || start > getEnd(text)))
			{
				return false;
			}

			int pos = 1;
			for (LeadingMarginSpan bulletSpan2 : mSpans)
			{
				int spanStart = text.getSpanStart(bulletSpan2);
				int spanEnd = text.getSpanEnd(bulletSpan2);
				if (end < spanStart || start > spanEnd)
				{
				}
				else
				{
					mSpans.add(pos, span);
					return true;
				}

				pos++;
			}

			return false;
		}

		public int getStart(Editable text)
		{
			if (mSpans != null && mSpans.size() > 0)
			{
				LeadingMarginSpan firstBulletSpan = mSpans.get(0);
				int start = text.getSpanStart(firstBulletSpan);
				return start;
			}

			return -1;
		}

		public int getEnd(Editable text)
		{
			if (mSpans != null && mSpans.size() > 0)
			{
				LeadingMarginSpan lastBulletSpan = mSpans.get(mSpans.size() - 1);
				int end = text.getSpanEnd(lastBulletSpan);
				return end;
			}

			return -1;
		}

		public String toHtml(Editable text)
		{
			return "";
		}
	}

	class Ul extends HtmlList
	{
		public String toHtml(Editable text)
		{
			String html = "";
			for (LeadingMarginSpan bulletSpan : mSpans)
			{
				int start = text.getSpanStart(bulletSpan);
				int end = text.getSpanEnd(bulletSpan);
				Editable tempEditable = (Editable) text.subSequence(start, end);
				String str = Html.toHtml(tempEditable).trim();

				StringBuilder sb = new StringBuilder(str);
				if (str.startsWith("<p>") && str.endsWith("</p>"))
				{
					sb.replace(sb.indexOf("<p>"), sb.indexOf("<p>") + "<p>".length(), "");
					sb.replace(sb.lastIndexOf("</p>"), sb.lastIndexOf("</p>") + "</p>".length(), "");
				}

				str = sb.toString();
				while (str.endsWith("\n"))
				{
					str = str.substring(0, str.length() - 1);
				}

				str = "<li>" + str + "</li>";
				html += str;
			}

			html = "<ul class=\"list-paddingleft-2\" style=\"list-style-type: disc;\">" + html
					+ "</ul>";
			return html;
		}
	}

	class Ol extends HtmlList
	{
		@Override
		public String toHtml(Editable text)
		{
			String html = "";
			for (LeadingMarginSpan bulletSpan : mSpans)
			{
				int start = text.getSpanStart(bulletSpan);
				int end = text.getSpanEnd(bulletSpan);
				Editable tempEditable = (Editable) text.subSequence(start, end);
				String str = Html.toHtml(tempEditable).trim();

				StringBuilder sb = new StringBuilder(str);
				if (str.startsWith("<p>") && str.endsWith("</p>"))
				{
					sb.replace(sb.indexOf("<p>"), sb.indexOf("<p>") + "<p>".length(), "");
					sb.replace(sb.lastIndexOf("</p>"), sb.lastIndexOf("</p>") + "</p>".length(), "");
				}

				str = sb.toString();

				str = "<li>" + str + "</li>";
				html += str;
			}

			html = "<ol class=\"list-paddingleft-2\" style=\"list-style-type: decimal;\">" + html
					+ "</ol>";
			return html;
		}
	}
}
