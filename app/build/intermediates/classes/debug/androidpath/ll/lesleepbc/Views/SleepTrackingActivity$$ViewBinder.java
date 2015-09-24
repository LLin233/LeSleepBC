// Generated code from Butter Knife. Do not modify!
package androidpath.ll.lesleepbc.Views;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SleepTrackingActivity$$ViewBinder<T extends androidpath.ll.lesleepbc.Views.SleepTrackingActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131492977, "field 'mStatusRecording'");
    target.mStatusRecording = finder.castView(view, 2131492977, "field 'mStatusRecording'");
    view = finder.findRequiredView(source, 2131492971, "field 'toolbar'");
    target.toolbar = finder.castView(view, 2131492971, "field 'toolbar'");
    view = finder.findRequiredView(source, 2131492975, "field 'fab'");
    target.fab = finder.castView(view, 2131492975, "field 'fab'");
  }

  @Override public void unbind(T target) {
    target.mStatusRecording = null;
    target.toolbar = null;
    target.fab = null;
  }
}
