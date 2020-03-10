import glob
import os 
from subprocess import Popen, PIPE
import shlex

base_dir = "/data/E0/AndroidMal/Datasets/Malware/"
db_path = base_dir + "AMD"
cg_output = "/data/E0/AndroidMal/callgraph_flowdroid/sootOutput/"

def list_all_files(rootdir):
    import os
    _files = []
    list = os.listdir(rootdir)
    for i in range(0,len(list)):
           path = os.path.join(rootdir,list[i])
           if os.path.isdir(path):
               _files.extend(list_all_files(path))
           if os.path.isfile(path):
               _files.append(path)
    return _files

apks = list_all_files(db_path)
with open("analyse_order.txt", "w") as f:
    f.writelines(apks)
for app in apks:
    outputdir = "/".join(app.split(base_dir)[1].split('/')[:-1])
    if not os.path.isdir(cg_output + outputdir):
        os.makedirs(cg_output + outputdir)
    outfile = os.path.join(cg_output + outputdir, app.split('/')[-1].split(".")[0] + ".dot")
    print(outfile)
    if not os.path.isfile(outfile):
        cmd = "java -cp .:/data/E0/AndroidMal/callgraph_flowdroid/soot-infoflow-cmd-jar-with-dependencies.jar App " + app + " /usr/lib/android-sdk/platforms " + outputdir
        ran = Popen(shlex.split(cmd))
        while 1:
            check = Popen.poll(ran) 
            if check is not None:		#check if process is still running
                break
        print(ran.communicate())
    else:
        print("already analysed!")
    with open("breakpoint.txt", "w") as f:
        f.write(app)