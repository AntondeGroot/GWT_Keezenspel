package adg.keezen.util;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class LanguageSelectorWidget extends Composite {

    /**
     * @param reloadOnChange true  → reload the page immediately (use in lobby / non-game pages)
     *                       false → save preference only; reload happens on next page load
     *                               (use during an active game to avoid disrupting the session)
     */
    public LanguageSelectorWidget(boolean reloadOnChange) {
        FlowPanel container = new FlowPanel();
        container.setStyleName("lang-selector-container");

        Label icon = new Label("🌐");
        icon.setStyleName("lang-selector-icon");

        ListBox listBox = new ListBox();
        listBox.setStyleName("lang-listbox");

        Language current = Cookie.getLanguage();
        for (Language lang : Language.values()) {
            listBox.addItem(lang.getDisplayName(), lang.name());
            if (lang == current) {
                listBox.setSelectedIndex(listBox.getItemCount() - 1);
            }
        }

        listBox.addChangeHandler(e -> {
            String selected = listBox.getValue(listBox.getSelectedIndex());
            Language chosen = Language.valueOf(selected);
            if (reloadOnChange) {
                Cookie.changeLanguage(chosen);   // saves + reloads
            } else {
                Cookie.setLanguage(chosen);      // saves only; applied on next load
            }
        });

        container.add(icon);
        container.add(listBox);
        initWidget(container);
    }
}