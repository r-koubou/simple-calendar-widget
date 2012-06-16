
package org.rkoubou.calendarwidget;

import java.util.Calendar;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.MonthDisplayHelper;
import android.widget.RemoteViews;

/**
 * カレンダーのビュー（ビットマップ）を生成、保持をする。
 * @author あーる
 */
public class CalendarViewRenderer
{
    static private final int COL   = 7;
    static private final int ROW   = 7;
    static private final int ALPHA = 0x40000000;

    static private final int[][] COLORS =
    {
        { 0xffff0000, 0xff0000 | ALPHA, },  // RED
        { 0xff000000, 0x000000 | ALPHA, },  // BLACK
        { 0xff0000ff, 0x0000ff | ALPHA, },  // BLUE
    };

    // 日にちが決まっている祝日
    static private final int[][] HOLIDAY1 =
    {
        /* 01月 */ { 1, },
        /* 02月 */ { 11, },
        /* 03月 */ {},   // 春分の日は計算で算出
        /* 04月 */ { 29 },
        /* 05月 */ { 3, 4, 5 },
        /* 06月 */ {},
        /* 07月 */ {},
        /* 08月 */ {},
        /* 09月 */ {},   // 秋分の日は計算で算出
        /* 10月 */ {},
        /* 11月 */ { 3, 23 },
        /* 12月 */ { 23 },
    };

    // 日にちが決まっていない祝日
    static private final int[][] HOLIDAY2 =
    {
        // 月, 第何, 曜日
        /* 成人の日 */  { 1, 2, 1 },
        /* 海の日 */    { 7, 3, 1 },
        /* 敬老の日 */  { 9, 3, 1 },
        /* 体育の日 */  { 10, 2, 1 },
    };

    private Bitmap bitmap;
    private int width;
    private int height;
    private int gridW;
    private int gridH;

    //////////////////////////////////////////////////////////////////////////
    /**
     * 初期化
     */
    synchronized public void init( Context ctx, AppWidgetProviderInfo info )
    {
        dispose();

        float scale = ctx.getResources().getDisplayMetrics().density;
        int dipW    = info.minWidth;
        int dipH    = info.minHeight;
        int width_  = (int)( dipW * scale + 0.5f );
        int height_ = (int)( dipH * scale + 0.5f );

        width_  = width_  - ( width_  % COL );
        height_ = height_ - ( height_ % ROW );
        bitmap = Bitmap.createBitmap( width_, height_, Config.RGB_565 );

        this.width  = width_;
        this.height = height_;
        this.gridW  = width_  / COL;
        this.gridH  = height_ / COL;

    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * メモリ開放
     */
    synchronized public void dispose()
    {
        if( bitmap != null )
        {
            bitmap.recycle();
            bitmap = null;
            width  = 0;
            height = 0;
        }
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * 利用可能状態かどうかを判定する。
     */
    synchronized public boolean ready()
    {
        return bitmap != null;
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * プロバイダから定期的呼び出される処理。
     */
    synchronized public void update( RemoteViews target, int viewId )
    {

        if( !ready() )
        {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int year  = calendar.get( Calendar.YEAR );
        int month = calendar.get( Calendar.MONTH );

//*
        MonthDisplayHelper monthDisplayHelper = new MonthDisplayHelper( year, month );
/*/
        // for test
        MonthDisplayHelper monthDisplayHelper = new MonthDisplayHelper( 2012, 8 );
/**/
        render( calendar, monthDisplayHelper );
        target.setImageViewBitmap( viewId, bitmap );
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * ビューのレンダリング。
     */
    private void render( Calendar calendar, MonthDisplayHelper monthDisplayHelper )
    {
        Canvas cvs  = new Canvas( bitmap );

        Paint p = new Paint();
        p.setAntiAlias( false );
        p.setStyle( Style.FILL );
        p.setColor( Color.WHITE );
        cvs.drawRect( 0, 0, width, height, p );

        p.setColor( 0xffeeffd2 );
        cvs.drawRect( 0, 0, width, gridH, p );

        drawMonth( cvs, p, monthDisplayHelper );
        drawDate( cvs, p, monthDisplayHelper, calendar.get( Calendar.DAY_OF_MONTH ) );

        p.setStyle( Style.FILL );
        p.setColor( Color.rgb( 0x80, 0x80, 0x80 ) );
        for( int i = 1; i < ROW; i++ )
        {
            for( int j = 1; j < COL; j++ )
            {
                cvs.drawLine( 0, i * gridH, width, i * gridH,  p );
                cvs.drawLine( j * gridW, gridH, j * gridW, height, p );
            }
        }

        p.setStyle( Style.STROKE );
        p.setColor( Color.BLACK );
        cvs.drawRect( 1, 1, width - 1 , height - 1, p );
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * 月のレンダリング
     */
    private void drawMonth( Canvas g, Paint p, MonthDisplayHelper monthDisplayHelper )
    {
        String[] m =
        {
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec",
        };

        int f = gridW - ( gridW / 2 );

        p.setTextSize( f );
        p.setAntiAlias( true );
        p.setTypeface( Typeface.DEFAULT );

        FontMetricsInt font = p.getFontMetricsInt();
        String text = m[ monthDisplayHelper.getMonth() ] + " " + monthDisplayHelper.getYear();

        int strH = Math.abs( font.top ) + font.bottom;
        int strB = Math.abs( font.top );

        int sw = (int)p.measureText( text );
        int dx = ( width / 2 ) - ( sw / 2 );
        int dy = ( 0 * gridH ) + ( gridH / 2 ) - ( strH / 2 ) + strB;

        drawText( g, text, dx, dy, COLORS[ 1 ], p );
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * 日のレンダリング
     */
    private void drawDate( Canvas g, Paint p, MonthDisplayHelper monthDisplayHelper, int dayOfMonth )
    {
        int f = gridW - ( gridW / 2 );

        p.setTextSize( f );
        p.setAntiAlias( true );
        p.setTypeface( Typeface.MONOSPACE );

        int year  = monthDisplayHelper.getYear();
        int month = monthDisplayHelper.getMonth();

        // 第何週かをカウントアップする
        int[] countOfWeek = new int[ 7 ];

        // 振替休日判定
        boolean furikae = false;

        int color;
        int i, j;

        p.setStyle( Style.FILL );

        for( i = 0; i < 6; i++ )
        {
            for( j = 0; j < 7; j++ )
            {

                if( ! monthDisplayHelper.isWithinCurrentMonth( i, j ) )
                {
                    continue;
                }

                countOfWeek[ j ]++;

                int date = monthDisplayHelper.getDayAt( i, j );

                p.setColor( Color.WHITE );

                if( dayOfMonth == date )
                {
                    p.setColor( Color.rgb( 194, 221, 240 ) );
                }
                g.drawRect( j * gridW, ( gridH * ( i + 1) ), j * gridW + gridW, ( gridH * ( i + 1 ) ) + gridH, p );

                String t = String.valueOf( date );

                switch( j )
                {
                    case 0:  color = 0; break;
                    case 6:  color = 2; break;
                    default: color = 1; break;
                }

                if( furikae )
                {
                    furikae = false;
                    color = 0;
                }
                else
                {
                    if( hasHoliday1( year, month, date ) || hasHoliday2( month, countOfWeek[ j ], j ) )
                    {
                        color = 0;
                        if( j == 0 )
                        {
                            // 日曜なら次の月曜は振替休日
                            furikae = true;
                        }
                    }
                    // 国民の休日（祝日と祝日の間が平日の場合）
                    else
                    {
                        if( date > 1 && date < monthDisplayHelper.getNumberOfDaysInMonth() && j > 0 && j < 6 )
                        {
                            int cow1 = countOfWeek[ j - 1 ];
                            int cow2 = countOfWeek[ j + 1 ] + 1;

                            if( ( hasHoliday1( year, month, date - 1 ) || hasHoliday2( month, cow1, j - 1 ) ) &&
                                ( hasHoliday1( year, month, date + 1 ) || hasHoliday2( month, cow2, j + 1 ) ) )
                            {
                                color = 0;
                            }

                        }
                    }
                }

                drawText( g, t, j, i + 1, gridW, gridH, COLORS[ color ], p );
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     *
     */
    private void drawText( Canvas g, String text, int x, int y, int[] colors, Paint p )
    {
        p.setColor( colors[ 1 ] );
        g.drawText( text, x + 3, y + 3, p );

        p.setColor( colors[ 0 ] );
        g.drawText( text, x, y, p );
   }


    //////////////////////////////////////////////////////////////////////////
    /**
     *
     */
    private void drawText( Canvas g, String text, int col, int row, int cw, int rh, int[] colors, Paint p )
    {
        FontMetricsInt font = p.getFontMetricsInt();

        int strW = (int)p.measureText( text );
        int strH = Math.abs( font.top ) + font.bottom;
        int strB = Math.abs( font.top );

        int dx = ( col * cw ) + ( cw / 2 ) - ( strW / 2 );
        int dy = ( row * rh ) + ( rh / 2 ) - ( strH / 2 ) + strB;

        drawText( g, text, dx, dy, colors, p );
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * 祝日判定ルーチン（日付）
     */
    private boolean hasHoliday1( int year, int month, int date )
    {

        if( isShunbun( year, month, date ) ||
            isShuubun( year, month, date ) )
        {
            return true;
        }

        for( int m : HOLIDAY1[ month ] )
        {
            if( m == date )
            {
                return true;
            }
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * 祝日判定ルーチン（週、曜日）
     */
    private boolean hasHoliday2( int month, int countOfWeek, int day )
    {
        for( int[] m : HOLIDAY2 )
        {
            if( m[ 0 ] == month + 1   &&
                m[ 1 ] == countOfWeek &&
                m[ 2 ] == day )
            {
                return true;
            }
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * 春分の日判定
     */
    private boolean isShunbun( int year, int month, int date )
    {
        if( month == 2 )
        {
            int judgeDate = 0;

            /*
                西暦年数の4での剰余が0の場合
                　　1900年〜1956年までは3月21日
                　　1960年〜2088年までは3月20日
                　　2092年〜2096年までは3月19日
                西暦年数の4での剰余が1の場合
                　　1901年〜1989年までは3月21日
                　　1993年〜2097年までは3月20日
                西暦年数の4での剰余が2の場合
                　　1902年〜2022年までは3月21日
                　　2026年〜2098年までは3月20日
                西暦年数の4での剰余が3の場合
                　　1903年〜1923年までは3月22日
                　　1927年〜2055年までは3月21日
                　　2059年〜2099年までは3月20日
            */
            switch( year % 4 )
            {
                case 0:
                {
                    if( year >= 1900 && year <= 1956 ) { judgeDate = 21; };
                    if( year >= 1960 && year <= 2088 ) { judgeDate = 20; };
                    if( year >= 2092 && year <= 2096 ) { judgeDate = 19; };
                }
                break;
                case 1:
                {
                    if( year >= 1901 && year <= 1989 ) { judgeDate = 21; };
                    if( year >= 1993 && year <= 2097 ) { judgeDate = 20; };
                }
                break;
                case 2:
                {
                    if( year >= 1902 && year <= 2022 ) { judgeDate = 21; };
                    if( year >= 2026 && year <= 2098 ) { judgeDate = 20; };
                }
                break;
                case 3:
                {
                    if( year >= 1903 && year <= 1923 ) { judgeDate = 22; };
                    if( year >= 1927 && year <= 2055 ) { judgeDate = 21; };
                    if( year >= 2059 && year <= 2099 ) { judgeDate = 20; };
                }
                break;
            }

            return date == judgeDate;
        }

        return false;

    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * 秋分の日判定
     */
    private boolean isShuubun( int year, int month, int date )
    {
        if( month == 8 )
        {
            int judgeDate = 0;

            /*
            西暦年数の4での剰余が0の場合
            　　1900年〜2008年までは9月23日
            　　2012年〜2096年までは9月22日
            西暦年数の4での剰余が1の場合
            　　1901年〜1917年までは9月24日
            　　1921年〜2041年までは9月23日
            　　2045年〜2097年までは9月22日
            西暦年数の4での剰余が2の場合
            　　1902年〜1946年までは9月24日
            　　1950年〜2074年までは9月23日
            　　2078年〜2098年までは9月22日
            西暦年数の4での剰余が3の場合
            　　1903年〜1979年までは9月24日
            　　1983年〜2099年までは9月23日
            */
            switch( year % 4 )
            {
                case 0:
                {
                    if( year >= 1900 && year <= 2008 ) { judgeDate = 23; };
                    if( year >= 2012 && year <= 2096 ) { judgeDate = 22; };
                }
                break;
                case 1:
                {
                    if( year >= 1901 && year <= 1917 ) { judgeDate = 24; };
                    if( year >= 1921 && year <= 2041 ) { judgeDate = 23; };
                    if( year >= 2045 && year <= 2097 ) { judgeDate = 22; };
                }
                break;
                case 2:
                {
                    if( year >= 1902 && year <= 1946 ) { judgeDate = 24; };
                    if( year >= 1950 && year <= 2074 ) { judgeDate = 23; };
                    if( year >= 2078 && year <= 2098 ) { judgeDate = 22; };
                }
                break;
                case 3:
                {
                    if( year >= 1903 && year <= 1979 ) { judgeDate = 24; };
                    if( year >= 1983 && year <= 2099 ) { judgeDate = 23; };
                }
                break;
            }

            return date == judgeDate;
        }

        return false;
    }
}
