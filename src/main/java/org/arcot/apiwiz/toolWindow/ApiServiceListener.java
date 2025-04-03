package org.arcot.apiwiz.toolWindow;

import com.intellij.execution.services.ServiceEventListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiServiceListener implements ServiceEventListener {
    private final Project project;
    private final JTextArea responseArea;
    private final JTextField urlField;
    private final JTree collectionTree;
    private static final Pattern URL_PATTERN = Pattern.compile("Running on (http://[^\\s]+)");

    public ApiServiceListener(Project project, JTextArea responseArea, JTextField urlField, JTree collectionTree) {
        this.project = project;
        this.responseArea = responseArea;
        this.urlField = urlField;
        this.collectionTree = collectionTree;
    }

    @Override
    public void handle(@NotNull ServiceEvent event) {
        // Check for both Flask and FastAPI
        if (isFlaskOrFastAPI(event)) {
            handleApiStartup("http://127.0.0.1:8000", isFlask(event));
        }
    }

    private boolean isFlaskOrFastAPI(ServiceEvent event) {
        // TODO: Implement proper detection
        return true;
    }

    private boolean isFlask(ServiceEvent event) {
        // TODO: Implement proper Flask detection
        return true;
    }

    private void handleApiStartup(String baseUrl, boolean isFlask) {
        SwingUtilities.invokeLater(() -> {
            urlField.setText(baseUrl);
            String framework = isFlask ? "Flask" : "FastAPI";
            responseArea.setText(framework + " application detected at: " + baseUrl + 
                "\n\nReady to send requests!");
            
            updateCollectionTree(baseUrl, isFlask);
        });
    }

    private void updateCollectionTree(String baseUrl, boolean isFlask) {
        DefaultTreeModel model = (DefaultTreeModel) collectionTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        
        String nodeName = isFlask ? "Flask APIs" : "FastAPI APIs";
        DefaultMutableTreeNode apiNode = findOrCreateNode(root, nodeName);
        
        DefaultMutableTreeNode endpointNode = new DefaultMutableTreeNode(
            new RequestNode("Default Endpoint", "GET", baseUrl)
        );
        apiNode.add(endpointNode);
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