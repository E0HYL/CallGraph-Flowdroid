import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;
import static soot.util.dot.DotGraph.DOT_EXTENSION;

public class App {
    /**
     * 
     * @param args args[0]: the apk file; args[1]: android.jar; args[2]: outputdir
     */
    public static void main(String[] args) {
        assert args.length == 2;
        String androidPlatform = args[1];
        String output = args[2];
        File file = new File(args[0]);
        String appToRun = file.getAbsolutePath();
        
        InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
        config.getAnalysisFileConfig().setAndroidPlatformDir(androidPlatform);
        config.getAnalysisFileConfig().setTargetAPKFile(appToRun);
        config.setMergeDexFiles(true);

        SetupApplication analyzer = new SetupApplication(config);
        analyzer.constructCallgraph();
        CallGraph cg = Scene.v().getCallGraph();
        // System.out.println(cg.toString());

        DotGraph dot = new DotGraph("callgraph");
        analyzeCG(dot, cg);
        String dest = (new File(appToRun)).getName();
        String fileNameWithOutExt = FilenameUtils.removeExtension(dest);
        String destination = "./sootOutput/" + output + "/" + fileNameWithOutExt;
        dot.plot(destination + DOT_EXTENSION);

    }
    
    /**
     * Iterate over the call Graph by visit edges one by one.
     * @param dot dot instance to create a dot file
     * @param cg call graph
     */
    public static void analyzeCG(DotGraph dot, CallGraph cg) {
        QueueReader<Edge> edges = cg.listener();
        Set<String> visited = new HashSet<>();

        File resultFile = new File("./CG.log");
        PrintWriter out = null;
        try {
            out = new PrintWriter(resultFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert out != null;
        out.println("=========CG begins=========");
        // iterate over edges of the call graph
        while (edges.hasNext()) {
            Edge edge = edges.next();
            SootMethod target = (SootMethod) edge.getTgt();
            MethodOrMethodContext src = edge.getSrc();
            if (!visited.contains(src.toString())) {
                dot.drawNode(src.toString());
                visited.add(src.toString());
            }
            if (!visited.contains(target.toString())) {
                dot.drawNode(target.toString());
                visited.add(target.toString());
            }
            out.println(src + "   ==>   " + target);
            dot.drawEdge(src.toString(), target.toString());
        }

        out.println("=========CG ends=========");
        out.close();
        System.out.println(cg.size());
    }
}
