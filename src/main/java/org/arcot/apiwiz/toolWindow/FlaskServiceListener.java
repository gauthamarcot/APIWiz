package org.arcot.apiwiz.toolWindow;

import com.intellij.execution.services.ServiceEventListener;
import com.intellij.openapi.project.Project;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlaskServiceListener implements ServiceEventListener {
    private final Project project;
    private final JTextArea responseArea;
    private final JTextField urlField;
    private final JTree collectionTree;
    private static final Pattern FLASK_URL_PATTERN = Pattern.compile("Running on (http://[^\\s]+)");

    public FlaskServiceListener(Project project, JTextArea responseArea, JTextField urlField, JTree collectionTree) {
        this.project = project;
        this.responseArea = responseArea;
        this.urlField = urlField;
        this.collectionTree = collectionTree;
    }

    @Override
    public void handle(@NotNull ServiceEvent event) {
        // For now, just detect Flask startup and use default URL
        handleFlaskStartup("http://127.0.0.1:5000");
    }

    private void handleFlaskStartup(String baseUrl) {
        SwingUtilities.invokeLater(() -> {
            urlField.setText(baseUrl);
            responseArea.setText("Flask application detected at: " + baseUrl + 
                "\n\nReady to send requests!");
            
            updateCollectionTree(baseUrl);
        });
    }

    private void updateCollectionTree(String baseUrl) {
        DefaultTreeModel model = (DefaultTreeModel) collectionTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        
        // Find or create Flask node
        DefaultMutableTreeNode flaskNode = findOrCreateNode(root, "Flask APIs");
        
        // Add the new endpoint
        DefaultMutableTreeNode endpointNode = new DefaultMutableTreeNode(baseUrl);
        flaskNode.add(endpointNode);
        model.reload();
    }

    private DefaultMutableTreeNode findOrCreateNode(DefaultMutableTreeNode parent, String name) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (name.equals(node.getUserObject())) {
                return node;
            }
        }
        
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);
        parent.add(newNode);
        return newNode;
    }
} 