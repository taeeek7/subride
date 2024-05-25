import os

def merge_java_files(src_directories, output_file):
    with open(output_file, 'w') as outfile:
        for src_directory in src_directories:
            for root, dirs, files in os.walk(src_directory):
                for file in files:
                    if file.endswith('.java'):
                        file_path = os.path.join(root, file)
                        try:
                            with open(file_path, 'r') as infile:
                                content = infile.read()
                                if content:
                                    outfile.write(f"// File: {file_path}\n")
                                    outfile.write(content)
                                    outfile.write("\n\n")
                                    print(f"Added {file_path}")
                                else:
                                    print(f"Skipped empty file: {file_path}")
                        except Exception as e:
                            print(f"Error reading {file_path}: {e}")

if __name__ == "__main__":
    src_directories = ['common', 'member']  # 소스 코드가 있는 디렉토리 리스트
    output_file = 'MergedSource.java'  # 병합된 소스를 저장할 파일 이름
    merge_java_files(src_directories, output_file)
    print(f"All Java files have been merged into {output_file}")

