import os
import subprocess
import zipfile

# List of files and directories to be compressed
files_and_directories = ["mod_info.json", "LICENSE", "data/", "graphics/", "jars/"]

# Function to get the latest Git tag
def get_latest_git_tag():
    try:
        # Using subprocess to execute git command
        completed_process = subprocess.run(["git", "describe", "--tags", "--abbrev=0"], capture_output=True, text=True, check=True)
        # The stdout attribute contains the output of the command
        latest_tag = completed_process.stdout.strip()
        return latest_tag
    except subprocess.CalledProcessError as e:
        # In case of an error (e.g., Git not installed, or not a Git repository)
        return f"Error: {e}"

# The name of the zip file
zip_filename = f"AITweaks_{get_latest_git_tag()}.zip"

# Create a ZipFile object in write mode
with zipfile.ZipFile(zip_filename, 'w', zipfile.ZIP_DEFLATED) as zipf:
    # Path for the internal directory in the zip file
    internal_dir = "AITweaks"

    for path in files_and_directories:
        if os.path.isdir(path):
            # If it's a directory, add the directory and all its contents
            for root, dirs, files in os.walk(path):
                # Add directory itself
                zipf.write(root, os.path.join(internal_dir, os.path.relpath(root, start=".")))

                for file in files:
                    file_path = os.path.join(root, file)
                    # Define the archive path for each file
                    archive_file_path = os.path.join(internal_dir, os.path.relpath(file_path, start="."))
                    zipf.write(file_path, archive_file_path)
        else:
            # If it's a file, just add it
            zipf.write(path, os.path.join(internal_dir, path))

