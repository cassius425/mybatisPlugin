import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.xml.XmlFile;

/**
 * Created by kong on 2017-11-2
 */
public class goToMapperAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        PsiElement psiElement = e.getData(PlatformDataKeys.PSI_ELEMENT);//鼠标所在的元素，这里就是方法
        if (psiElement == null) {
            return;
        }
        String methodName = psiElement.toString().replace("PsiMethod:", "");//获取到方法名

        PsiElement psiElementParent = psiElement.getParent();//获取方法的父元素
        if (psiElementParent == null) {
            return;
        }
        PsiFile containingFile = psiElementParent.getContainingFile();//获取到文件，这里是java类
        String className = containingFile.getName();//获取到类名

        String mapperName;
        if (className.endsWith("Service.java")) {
            mapperName = className.replace("Service.java", "Dao.xml");
        } else if (className.endsWith("Dao.java")) {
            mapperName = className.replace(".java", ".xml");
        } else {
            return;
        }

        Project project = e.getProject();
        //查找名称为mapperName的文件
        PsiFile[] files = PsiShortNamesCache.getInstance(project).getFilesByName(mapperName);
        if (files.length == 1) {
            XmlFile xmlFile = (XmlFile) files[0];
            String xml = xmlFile.getDocument().getText();//获取mapper xml字符串
            //判断mapper是否存id="methodName"的sql,存在就打开对应的mapper xml
            //这里判断比较简单，不严谨。可以通过XmlFile遍历节点判断是否存在
            if (StringUtil.isNotEmpty(xml) && xml.contains("id=\"" + methodName + "\"")) {
                toMapper(project, methodName, files[0].getVirtualFile(), xml);
            }
        }
    }

    /**
     * 进入mapper
     *
     * @param project
     * @param methodName
     * @param mapperFile
     * @param xml
     */
    private void toMapper(Project project, String methodName, VirtualFile mapperFile, String xml) {
        //打开xml文件
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, mapperFile);
        Editor editor = FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, true);

        //获取sql所在的行数，这里用了比较笨的方法。api找了很久没找到有什么方法可以获取行号，希望有大神指点
        String[] split = xml.split("\n");
        int lineNumber = 0;
        for (int i = 0; i < split.length; i++) {
            String line = split[i];
            if (StringUtil.isNotEmpty(line) && line.contains(methodName)) {
                lineNumber = i;
                break;
            }
        }
        //定位到对应的sql
        CaretModel caretModel = editor.getCaretModel();
        LogicalPosition logicalPosition = caretModel.getLogicalPosition();
        logicalPosition.leanForward(true);
        LogicalPosition logical = new LogicalPosition(lineNumber, logicalPosition.column);
        caretModel.moveToLogicalPosition(logical);
        SelectionModel selectionModel = editor.getSelectionModel();
        selectionModel.selectLineAtCaret();
    }
}
