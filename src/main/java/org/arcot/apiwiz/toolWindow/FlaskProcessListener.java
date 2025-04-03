package org.arcot.apiwiz.toolWindow;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlaskProcessListener extends ProcessAdapter {
    private final JTextArea responseArea;
    private final JTextField urlField;
    private final JTree collectionTree;
    private static final Pattern FLASK_URL_PATTERN = Pattern.compile("Running on (http://[^\\s]+)");

    public FlaskProcessListener(JTextArea responseArea, JTextField urlField, JTree collectionTree) {
        this.responseArea = responseArea;
        this.urlField = urlField;
        this.collectionTree = collectionTree;
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        String text = event.getText();
        if (text != null && text.contains("Running on")) {
            Matcher matcher = FLASK_URL_PATTERN.matcher(text);
            if (matcher.find()) {
                String baseUrl = matcher.group(1);
                SwingUtilities.invokeLater(() -> {
                    urlField.setText(baseUrl);
                    responseArea.setText("Flask application detected at: " + baseUrl + 
                        "\n\nReady to send requests!");
                    
                    // Add to collection tree
                    DefaultTreeModel model = (DefaultTreeModel) collectionTree.getModel();
                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                    DefaultMutableTreeNode flaskNode = null;
                    
                    // Find or create Flask node
                    for (int i = 0; i < root.getChildCount(); i++) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
                        if ("Flask APIs".equals(node.getUserObject())) {
                            flaskNode = node;
                            break;
                        }
                    }
                    
                    if (flaskNode == null) {
                        flaskNode = new DefaultMutableTreeNode("Flask APIs");
                        root.add(flaskNode);
                    }
                    
                    // Add the new endpoint
                    DefaultMutableTreeNode endpointNode = new DefaultMutableTreeNode(baseUrl);
                    flaskNode.add(endpointNode);
                    model.reload();
                });
            }
        }
    }
} 