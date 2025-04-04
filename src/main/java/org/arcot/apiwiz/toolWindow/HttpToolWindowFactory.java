package org.arcot.apiwiz.toolWindow;

import com.intellij.execution.services.ServiceEventListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

// Fix Swing imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpToolWindowFactory implements ToolWindowFactory {
    private JTextArea responseArea;
    private JTextField urlField;
    private JComboBox<String> methodCombo;
    private JTextArea requestBodyArea;
    private JTextArea headersArea;
    private JTree collectionTree;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);

        // Left side - Collections
        JPanel collectionsPanel = createCollectionsPanel();
        splitPane.setLeftComponent(collectionsPanel);

        // Right side - Request/Response
        JPanel mainPanel = createRequestResponsePanel();
        splitPane.setRightComponent(mainPanel);

        // Add service listener
        project.getMessageBus().connect().subscribe(
            ServiceEventListener.TOPIC,
            new ApiServiceListener(project, responseArea, urlField, collectionTree)
        );

        Content content = ContentFactory.getInstance().createContent(splitPane, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private JPanel createCollectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Collection toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newCollectionBtn = new JButton("New Collection");
        JButton importBtn = new JButton("Import");
        JButton exportBtn = new JButton("Export");
        
        toolbarPanel.add(newCollectionBtn);
        toolbarPanel.add(importBtn);
        toolbarPanel.add(exportBtn);

        // Tree with root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Collections");
        collectionTree = new JTree(root);
        
        // Add default categories
        DefaultMutableTreeNode flaskNode = new DefaultMutableTreeNode("Flask APIs");
        DefaultMutableTreeNode fastApiNode = new DefaultMutableTreeNode("FastAPI APIs");
        root.add(flaskNode);
        root.add(fastApiNode);

        // Tree popup menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem addRequest = new JMenuItem("Add Request");
        JMenuItem addFolder = new JMenuItem("Add Folder");
        JMenuItem rename = new JMenuItem("Rename");
        JMenuItem delete = new JMenuItem("Delete");
        
        popupMenu.add(addRequest);
        popupMenu.add(addFolder);
        popupMenu.add(rename);
        popupMenu.add(delete);

        collectionTree.setComponentPopupMenu(popupMenu);

        // Add action listeners
        newCollectionBtn.addActionListener(e -> createNewCollection());
        importBtn.addActionListener(e -> importCollection());
        exportBtn.addActionListener(e -> exportCollection());
        
        addRequest.addActionListener(e -> addRequestToCollection());
        addFolder.addActionListener(e -> addFolderToCollection());
        rename.addActionListener(e -> renameCollectionItem());
        delete.addActionListener(e -> deleteCollectionItem());

        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(collectionTree), BorderLayout.CENTER);

        return panel;
    }

    private void createNewCollection() {
        String name = JOptionPane.showInputDialog("Enter collection name:");
        if (name != null && !name.trim().isEmpty()) {
            DefaultTreeModel model = (DefaultTreeModel) collectionTree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);
            root.add(newNode);
            model.reload();
        }
    }

    private void importCollection() {
        // TODO: Implement collection import from Postman/other formats
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            // Handle import
        }
    }

    private void exportCollection() {
        // TODO: Implement collection export
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            // Handle export
        }
    }

    private void addRequestToCollection() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
            collectionTree.getLastSelectedPathComponent();
        if (node != null) {
            String name = JOptionPane.showInputDialog("Enter request name:");
            if (name != null && !name.trim().isEmpty()) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
                    new RequestNode(name, "GET", "")
                );
                node.add(newNode);
                ((DefaultTreeModel) collectionTree.getModel()).reload(node);
            }
        }
    }

    private void addFolderToCollection() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
            collectionTree.getLastSelectedPathComponent();
        if (node != null) {
            String name = JOptionPane.showInputDialog("Enter folder name:");
            if (name != null && !name.trim().isEmpty()) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);
                node.add(newNode);
                ((DefaultTreeModel) collectionTree.getModel()).reload(node);
            }
        }
    }

    private void renameCollectionItem() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
            collectionTree.getLastSelectedPathComponent();
        if (node != null) {
            String name = JOptionPane.showInputDialog("Enter new name:", 
                node.getUserObject().toString());
            if (name != null && !name.trim().isEmpty()) {
                node.setUserObject(name);
                ((DefaultTreeModel) collectionTree.getModel()).nodeChanged(node);
            }
        }
    }

    private void deleteCollectionItem() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
            collectionTree.getLastSelectedPathComponent();
        if (node != null && node.getParent() != null) {
            ((DefaultTreeModel) collectionTree.getModel()).removeNodeFromParent(node);
        }
    }

    private JPanel createRequestResponsePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel for URL and method
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        
        // Method combo and URL in one panel
        JPanel urlMethodPanel = new JPanel(new BorderLayout(5, 0));
        methodCombo = new JComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE"});
        methodCombo.setPreferredSize(new Dimension(100, methodCombo.getPreferredSize().height));
        urlField = new JTextField();
        urlMethodPanel.add(methodCombo, BorderLayout.WEST);
        urlMethodPanel.add(urlField, BorderLayout.CENTER);
        
        // Send button
        JButton sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(70, sendButton.getPreferredSize().height));
        
        // Combine URL/method and send button
        topPanel.add(urlMethodPanel, BorderLayout.CENTER);
        topPanel.add(sendButton, BorderLayout.EAST);

        // Center panel for headers and body
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        
        // Headers panel
        JPanel headersPanel = new JPanel(new BorderLayout());
        headersPanel.setBorder(BorderFactory.createTitledBorder("Headers"));
        headersArea = new JTextArea(4, 40);
        headersPanel.add(new JScrollPane(headersArea), BorderLayout.CENTER);
        
        // Request body panel
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBorder(BorderFactory.createTitledBorder("Request Body"));
        requestBodyArea = new JTextArea(4, 40);
        bodyPanel.add(new JScrollPane(requestBodyArea), BorderLayout.CENTER);
        
        centerPanel.add(headersPanel);
        centerPanel.add(bodyPanel);

        // Response panel
        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.setBorder(BorderFactory.createTitledBorder("Response"));
        responseArea = new JTextArea(8, 40);
        responseArea.setEditable(false);
        responsePanel.add(new JScrollPane(responseArea), BorderLayout.CENTER);

        // Add all components
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(responsePanel, BorderLayout.SOUTH);

        // Add action listener to send button
        sendButton.addActionListener(e -> sendRequest());

        return mainPanel;
    }

    private void sendRequest() {
        try {
            String url = urlField.getText();
            String method = (String) methodCombo.getSelectedItem();
            String headers = headersArea.getText();
            String body = requestBodyArea.getText();

            HttpRequestSender.sendRequest(url, method, headers, body, responseArea);
        } catch (Exception e) {
            responseArea.setText("Error sending request: " + e.getMessage());
        }
    }
} 