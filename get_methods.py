import re
from os import listdir


def extract_methods(filename: str, min_len: int):
    
    diff = open(f'./{folder}/{filename}', 'r').read()
    methods = []
    in_method = False
    current_method = []
    initMethodRegex = r'\+\s*(public|protected|private|static|\s) +[\w\<\>\[\]]+\s+(\w+) *\([^\)]*\) *(\{?|[^;])'
    endMethodRegex = r'\+\s*}\s*$'
    for line in diff.split('\n'):
        # Check if the line starts with '+' and contains a Java method signature
        if re.match(initMethodRegex, line):
            # Start of a new method
            in_method = True
            current_method = [line[1:]]
        elif in_method and len(current_method) >= min_len and re.match(endMethodRegex, line.strip()):
            # End of the method with at least 4 lines
            current_method.append(line[1:])
            methods.append('\n'.join(current_method))
            in_method = False
        elif in_method and re.match(r'^\+', line):
            # Continue adding lines to the current method
            current_method.append(line[1:])
        elif in_method:
            # Method is too short, discard it
            in_method = False

    return methods


folder = 'befores'
filenames_list = listdir(folder)
min_clone_size = 4

for java_file in filenames_list:
    list_of_methods_java = extract_methods(java_file, min_clone_size)

    for i, method in enumerate(list_of_methods_java):
        open(f'{java_file}_method{i}.java', 'a').write(method)
