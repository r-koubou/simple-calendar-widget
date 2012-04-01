
package org.rkoubou.calendarwidget;

import org.rz.calendarwidget.R;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

/**
 * カレンダーウィジェット。
 * @author あーる
 */
public class CalendarWidgetProvider extends AppWidgetProvider
{
    private final CalendarViewRenderer calendarViewCreator = new CalendarViewRenderer();

    //////////////////////////////////////////////////////////////////////////
    /**
     * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
     */
    @Override
    public void onUpdate( Context context, AppWidgetManager awm, int[] appWidgetIds )
    {
        super.onUpdate( context, awm, appWidgetIds );

        if( ! calendarViewCreator.ready() )
        {
            calendarViewCreator.init( context, awm.getAppWidgetInfo( appWidgetIds[ 0 ] ) );
        }

        ComponentName componentName = new ComponentName( context, CalendarWidgetProvider.class );
        RemoteViews view            = new RemoteViews( context.getPackageName(), R.layout.main );

        calendarViewCreator.update( view, R.id.screen );

        awm.updateAppWidget( componentName, view );
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * @see android.appwidget.AppWidgetProvider#onDisabled(android.content.Context)
     */
    @Override
    public void onDisabled( Context context )
    {
        super.onDisabled( context );
        calendarViewCreator.dispose();
    }
}
