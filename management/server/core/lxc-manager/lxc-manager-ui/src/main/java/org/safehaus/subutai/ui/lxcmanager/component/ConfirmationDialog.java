package org.safehaus.subutai.ui.lxcmanager.component;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.*;

/**
 * Created by daralbaev on 08.06.14.
 */
public class ConfirmationDialog {
    private Window alert;
    private Button cancel, ok;
    private VerticalLayout l;

    public ConfirmationDialog(String caption, String yesLabel, String cancelLabel) {
        l = new VerticalLayout();
        l.setWidth("200px");
        l.setMargin(true);
        l.setSpacing(true);

        alert = new Window(caption, l);
        alert.setModal(true);
        alert.setResizable(false);
        alert.setDraggable(false);
        alert.addStyleName("dialog");
        alert.setClosable(false);

        cancel = new Button(cancelLabel);
        ok = new Button(yesLabel);
    }

    public Window getAlert(){

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidth("100%");
        buttons.setSpacing(true);
        l.addComponent(buttons);

        cancel.addStyleName("small");
        cancel.addStyleName("wide");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                alert.close();
            }
        });
        buttons.addComponent(cancel);


        ok.addStyleName("default");
        ok.addStyleName("small");
        ok.addStyleName("wide");
        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                alert.close();
            }
        });
        buttons.addComponent(ok);
        ok.focus();

        alert.addShortcutListener(new ShortcutListener("Cancel",
                ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                alert.close();
            }
        });

        return alert;
    }

    public Button getOk() {
        return ok;
    }
}
