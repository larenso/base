package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.environment.ui.wizard.Blueprint2PeerGroupWizard;
import org.safehaus.subutai.core.environment.ui.wizard.Node2PeerWizard;
import org.safehaus.subutai.core.environment.ui.wizard.NodeGroup2PeerGroupWizard;
import org.safehaus.subutai.core.environment.ui.wizard.NodeGroup2PeerWizard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


@SuppressWarnings("serial")
public class BlueprintsForm
{

    private static final String NO_BLUEPRINTS = "No blueprints found";
    private static final String N2P = "N2P";
    private static final String B2PG = "B2PG";
    private static final String NG2PG = "NG2PG";
    private static final String NG2P = "NG2P";
    private static final String DELETE = "Delete";
    private static final String VIEW = "View";
    private static final String NAME = "Name";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerPortalModule module;
    private Button environmentsButton;


    public BlueprintsForm( EnvironmentManagerPortalModule module )
    {
        this.module = module;
        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        environmentsTable = createTable( "Blueprints", 300 );

        environmentsButton = new Button( VIEW );
        environmentsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                updateTableData();
            }
        } );

        contentRoot.addComponent( environmentsButton );
        contentRoot.addComponent( environmentsTable );
    }


    private Table createTable( String caption, int size )
    {
        Table table = new Table( caption );
        table.addContainerProperty( NAME, String.class, null );
        table.addContainerProperty( VIEW, Button.class, null );
        table.addContainerProperty( N2P, Button.class, null );
        table.addContainerProperty( NG2P, Button.class, null );
        table.addContainerProperty( B2PG, Button.class, null );
        table.addContainerProperty( NG2PG, Button.class, null );
        table.addContainerProperty( DELETE, Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }


    private void updateTableData()
    {
        environmentsTable.removeAllItems();
        List<EnvironmentBuildTask> tasks = module.getEnvironmentManager().getBlueprints();
        if ( !tasks.isEmpty() )
        {
            for ( final EnvironmentBuildTask task : tasks )
            {

                final Button view = new Button( VIEW );
                view.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        Window window = blueprintDetails( task );
                    }
                } );

                final Button N2P_BTN = new Button( N2P );
                N2P_BTN.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        Node2PeerWizard node2PeerWizard = new Node2PeerWizard( N2P, module, task );
                        contentRoot.getUI().addWindow( node2PeerWizard );
                        node2PeerWizard.setVisible( true );
                    }
                } );

                final Button B2PG_BTN = new Button( B2PG );
                B2PG_BTN.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        Blueprint2PeerGroupWizard node2PeerWizard = new Blueprint2PeerGroupWizard( B2PG, module, task );
                        contentRoot.getUI().addWindow( node2PeerWizard );
                        node2PeerWizard.setVisible( true );
                    }
                } );

                final Button NG2PG_BTN = new Button( NG2PG );
                NG2PG_BTN.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        NodeGroup2PeerGroupWizard node2PeerWizard =
                                new NodeGroup2PeerGroupWizard( NG2PG, module, task );
                        contentRoot.getUI().addWindow( node2PeerWizard );
                        node2PeerWizard.setVisible( true );
                    }
                } );

                final Button NG2P_BTN = new Button( NG2P );
                NG2P_BTN.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        NodeGroup2PeerWizard node2PeerWizard = new NodeGroup2PeerWizard( NG2P, module, task );
                        contentRoot.getUI().addWindow( node2PeerWizard );
                        node2PeerWizard.setVisible( true );
                    }
                } );

                final Button delete = new Button( DELETE );
                delete.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        module.getEnvironmentManager().deleteBlueprint( task.getUuid().toString() );
                        environmentsButton.click();
                    }
                } );

                environmentsTable.addItem( new Object[] {
                        task.getEnvironmentBlueprint().getName(), view, N2P_BTN, NG2P_BTN, B2PG_BTN, NG2PG_BTN, delete
                }, null );
            }
        }
        else
        {
            Notification.show( NO_BLUEPRINTS );
        }
        environmentsTable.refreshRowCache();
    }


    private Window blueprintDetails( final EnvironmentBuildTask task )
    {
        Window window = createWindow( "Blueprint details" );
        TextArea area = new TextArea();
        area.setSizeFull();
        area.setValue( GSON.toJson( task.getEnvironmentBlueprint() ) );
        window.setContent( area );
        contentRoot.getUI().addWindow( window );
        return window;
    }


    private Window createWindow( String caption )
    {
        Window window = new Window();
        window.setCaption( caption );
        window.setWidth( "800px" );
        window.setHeight( "600px" );
        window.setModal( true );
        window.setClosable( true );
        return window;
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
