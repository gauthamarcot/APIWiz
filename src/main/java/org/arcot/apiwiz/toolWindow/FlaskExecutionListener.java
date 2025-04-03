package org.arcot.apiwiz.toolWindow;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.*;

public class FlaskExecutionListener implements ExecutionListener {
    private final JTextArea responseArea;
    private final JTextField urlField;
    private final JTree collectionTree;

    public FlaskExecutionListener(JTextArea responseArea, JTextField urlField, JTree collectionTree) {
        this.responseArea = responseArea;
        this.urlField = urlField;
        this.collectionTree = collectionTree;
    }

    @Override
    public void processStarted(@NotNull String executorId,
                             @NotNull ExecutionEnvironment env,
                             @NotNull ProcessHandler handler) {
        handler.addProcessListener(new FlaskProcessListener(responseArea, urlField, collectionTree));
    }
} 