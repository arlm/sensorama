# Sensorama

#### Android version

For initializing the Parse API, you must register with Parse and provide 2
variables: `PARSE_API_ID` and `PARSE_CLIENT_ID`. If you intend to run with
Travis CI, set these 2 variables with the following commands:

	travis env set PARSE_API_ID
	travis env set PARSE_CLIENT_ID

To build the application, run:

	./build.sh

