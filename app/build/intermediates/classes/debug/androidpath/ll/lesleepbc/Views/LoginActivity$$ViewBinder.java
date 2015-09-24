// Generated code from Butter Knife. Do not modify!
package androidpath.ll.lesleepbc.Views;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class LoginActivity$$ViewBinder<T extends androidpath.ll.lesleepbc.Views.LoginActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131492967, "field 'mUsername'");
    target.mUsername = finder.castView(view, 2131492967, "field 'mUsername'");
    view = finder.findRequiredView(source, 2131492968, "field 'mPassword'");
    target.mPassword = finder.castView(view, 2131492968, "field 'mPassword'");
    view = finder.findRequiredView(source, 2131492969, "method 'login'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.login();
        }
      });
  }

  @Override public void unbind(T target) {
    target.mUsername = null;
    target.mPassword = null;
  }
}
