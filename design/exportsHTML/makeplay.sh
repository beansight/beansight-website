echo "Enter the name of the folder:"
read -e FOLDER
find ./ -type f -name '*.css' -exec sed -i 's/\.\.\/images/\/public\/images/g' {} \;
