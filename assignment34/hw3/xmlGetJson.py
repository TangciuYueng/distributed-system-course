import re
import json
import os

data={}
dir_path = 'E:\\xx\\distributed_system\\assignment34\\hw3'
file_path='E:\\xx\\distributed_system\\assignment34\\hw3\\test.xml'

with open(file_path, 'r', encoding='utf-8') as f:
    author=[]
    for line in f.readlines():
        if "key=" in line:
            authors=[]
        if "<author" in line:
            searchObj=re.search(r'<author([\s\S]*?)>([\s\S]*?)</author>',line,re.M|re.I)
            if searchObj:
                authors.append(searchObj.group(2))
        if "<year>" in line:
            searchObj=re.search(r'<year>([\d]*?)</year>',line,re.M|re.I)
            if searchObj:
                year=int(searchObj.group(1))
                for author in authors:
                    #data["author"]
                    if data.__contains__(author):
                        if data[author].__contains__(year):
                            data[author][year]+=1
                        else:
                            data[author][year]=1
                    else:
                        data[author]={}
                        data[author][year]=1

    print(data)
    json_data = json.dumps(data,indent=2,sort_keys=True)
    print(json_data)

    file_name_without_extension = os.path.splitext(f.name)[0]
    new_file_path = os.path.join(dir_path, file_name_without_extension + '.json')

    f2=open(new_file_path, 'w')
    f2.write(json_data)
    f2.close()

    print(f.name +" complete preprocess!")

