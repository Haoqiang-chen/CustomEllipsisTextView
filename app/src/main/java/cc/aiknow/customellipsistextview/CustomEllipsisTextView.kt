package cc.aiknow.customellipsistextview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.TextView

/**
 *  变更:添加对省略符号样式的设置：如颜色、字体大小
 * -
 * - @chenhaoqiang
 * - @date 2018/11/2
 * @author chenhaoqiang
 * @since 2018-10-31 16:41
 * @version 1.0
 */
class CustomEllipsisTextView : TextView {
    private lateinit var typeArray: TypedArray
    private var ellipsis: String? = null
    private var limitLines: Int = DEFAULT_LIMIT_LINES
    private var ellipsisPosition: Int = 0
    private val sourceText: String = text.toString()
    private var ellipsisColor: Int = textColors.defaultColor
    private var ellipsisSizePx: Int = textSize.toInt()

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.CustomEllipsisTextView)
        ellipsis = typeArray.getString(R.styleable.CustomEllipsisTextView_ellipsis)
        limitLines = typeArray.getInteger(R.styleable.CustomEllipsisTextView_limitLines, DEFAULT_LIMIT_LINES)
        ellipsisPosition = typeArray.getInteger(R.styleable.CustomEllipsisTextView_ellipsisPosition, END)
        ellipsisColor = typeArray.getInteger(R.styleable.CustomEllipsisTextView_ellipsisColor, textColors.defaultColor)
        ellipsisSizePx = typeArray.getInteger(R.styleable.CustomEllipsisTextView_ellipsisSizePx, textSize.toInt())
        typeArray.recycle()
    }

    companion object {
        const val DEFAULT_LIMIT_LINES = 1
        const val END = 0
        const val MIDDLE = 1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 若文本长度小于被限制的行数则不需要显示省略号
        val lines = layout.lineCount
        if (lines < limitLines) {
            return
        }
        // 根据设置的省略号位置添加省略号
        when (ellipsisPosition) {
            END -> {
                ellipsis?.let {
                    var index = sourceText.length
                    var targetText = sourceText + it
                    var targetSpannable = setEllipsisStyle(targetText, ellipsisColor, ellipsisSizePx, targetText.length - it.length, targetText.length)
                    var staticLayout = StaticLayout(targetSpannable, layout.paint, layout.width, layout.alignment, layout.spacingMultiplier, layout.spacingAdd, false)
                    // 从后向前依次减少字符并判断在限制的行数中是否可以显示
                    while (staticLayout.lineCount > limitLines) {
                        index--
                        targetText = sourceText.substring(0, index) + it
                        targetSpannable = setEllipsisStyle(targetText, ellipsisColor, ellipsisSizePx, targetText.length - it.length, targetText.length)
                        staticLayout = StaticLayout(targetSpannable, layout.paint, layout.width, layout.alignment, layout.spacingMultiplier, layout.spacingAdd, false)
                    }
                    text = targetSpannable
                }
            }
            MIDDLE -> {
                ellipsis?.let {
                    val middle = sourceText.length / 2
                    var indexLeft = middle + 1 // subString左闭右开
                    var indexRight = middle + 1
                    var leftSubString = sourceText.substring(0, indexLeft)
                    var rightSubString = sourceText.substring(indexRight, sourceText.length)
                    var targetText = leftSubString + it + rightSubString
                    var targetSpannable = setEllipsisStyle(targetText, ellipsisColor, ellipsisSizePx, leftSubString.length, leftSubString.length + it.length)
                    var i = 0
                    // 使用StaticLayout来判断当前显示的字符串是否超出layout.width
                    var staticLayout = StaticLayout(targetSpannable, layout.paint, layout.width, layout.alignment, layout.spacingMultiplier, layout.spacingAdd, false)
                    // 截取左右子串并与省略字符拼接来判断是否可以在规定的行数(MIDDLE应为1行)中显示
                    while (staticLayout.lineCount > limitLines) {
                        // 若文字长度超出屏幕宽度或者产生换行则循环对左右子串进行裁剪
                        when (i % 2) {
                            0 -> {
                                indexLeft--
                                leftSubString = sourceText.substring(0, indexLeft)
                                targetText = leftSubString + it + rightSubString
                            }
                            1 -> {
                                indexRight++
                                rightSubString = sourceText.substring(indexRight, sourceText.length)
                                targetText = leftSubString + it + rightSubString
                            }
                        }
                        targetSpannable = setEllipsisStyle(targetText, ellipsisColor, ellipsisSizePx, leftSubString.length, leftSubString.length + it.length)
                        staticLayout = StaticLayout(targetSpannable, layout.paint, layout.width, layout.alignment, layout.spacingMultiplier, layout.spacingAdd, false)
                        i++
                    }
                    text = targetSpannable
                }
            }
        }
    }

    // 获取文字宽度
    private fun getTextWidth(text: String): Int {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return bounds.width()
    }

    // 设置省略号颜色、大小: 若相应属性为定义则使用与文本相同的默认值
    private fun setEllipsisStyle(text: String, color: Int, px: Int, start: Int, end: Int): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(text)
        spannable.setSpan(ForegroundColorSpan(ellipsisColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(AbsoluteSizeSpan(px), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    // 设置省略符号
    fun setEllipsis(ellipsis: String) {
        this.ellipsis = ellipsis
        requestLayout()
    }

    // 设置行数
    fun setLimitLines(limitLines: Int) {
        this.limitLines = limitLines
        requestLayout()
    }

    // 设置显示位置
    fun setEllipsisPosition(position: Int) {
        this.ellipsisPosition = position
        requestLayout()
    }

    // 设置省略符号颜色
    fun setEllipsisColor(color: Int) {
        this.ellipsisColor = color
        requestLayout()
    }

    // 设置省略符号大小
    fun setellipsisSizePx(px: Int) {
        this.ellipsisSizePx = px
        requestLayout()
    }
}