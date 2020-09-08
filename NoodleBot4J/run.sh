echo "Updating dependencies..."
./update.sh
echo "done."
echo "Building project..."
./build.sh
echo "done."

echo "Preparing test environment..."
if [ ! -d "testenv" ]; then
	mkdir testenv
fi
if [ ! -d "testenv/libs" ]; then
	rm -r testenv/libs
fi
if [ ! -d "testenv/filters" ]; then
	rm -r testenv/filters
fi
cp -r target/libs testenv
cp -r filters testenv
echo "Copying generated packages..."
if [ -f testenv/NoodleBot4J*.jar ]; then
	rm testenv/NoodleBot4J*.jar
fi
cp target/NoodleBot4J*.jar testenv
echo "done."

echo "Running project..."
cd testenv
java -jar NoodleBot4J*.jar
