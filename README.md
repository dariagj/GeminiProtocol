# Gemini Protocol
Created a client, server, and proxy based on the Gemini Protocol.

## Author
Daria Gjonbalaj


## Preparing the Environment 
It is expected to have installed:
- OpenJDK 23 or newer
- Maven 3.9 or newer (3.8 may also work)

## Installation
Building and installing the dependencies:
1. Clone the repository:
   - ```git clone https://github.com/dariagj/GeminiProtocol.git```
2. Make sure to navigate to the project directory
   - ```cd GeminiProtocol```
3. Install the dependencies, using Maven:
  - ```mvn install```

## Usage
1. Run Maven
   - ```mvn clean package``` 
2. Run Server
   - ```java -cp target/gemini-2025.jar gemini.Server <directory> [port]```
3. Run Proxy
   - ```java -cp target/gemini-2025.jar gemini.Proxy <port>```
4. Run Client
   - ```java -cp target/gemini-2025.jar gemini.Client <URL> [input]```

## More about the Client, Server, and Proxy

### Gemini Client Program
Command-line usage: `java -cp target/gemini-2025.jar gemini.Client <URL> [input]`.

Argument/s:
- `URL` is required because of how the client behaves, it needs to connect to a URL/URI with a scheme, host, and path.
- `input` is optional. It is used when the server returns a 1x status code (asking for input), so the client immediately passes it through without needing to ask the user again for it.

Valid formats: Anything that applies to the project specification (Gemini protocol).

Environment variable:
- `GEMINI_PROXY = host:port` → for using a proxy

Input Handling:
- If provided when running the `Client`, it is used and send to the server after the ask for input
- If not, the user through the console gives the input and the URL/URI is updated.

Slow Down (Status Code 44):
- If provided in the meta, the system waits for that many seconds
- If not, the system waits 1 second

Exit codes:
- 0 → success
- 1 → local error (URL not provided, malformed request, too many redirections, proxy connection failure (due to environment variable or port failure), malformed reply)
- 4x/5x → server error codes passed through

Example: `java -cp target/gemini-2025.jar gemini.Client gemini://localhost/README.gmi`


### Gemini Server Program

Command-line usage: `java -cp target/gemini-2025.jar gemini.Server <directory> [port]`

Argument/s:
- `directory` is required because the server needs to know the file system where to look up the files asked by the client.
  - there either needs to be an already made directory or make a new one for the purpose of testing
- `port` is optional. When provided, it is used instead of the default port (1958).

Valid formats: Anything that applies to the project specification (Gemini protocol).

Status Codes:
- 1x → input needed
- 2x → success
- 3x → redirection (absolute/relative)
- 44 → sleep
- 4x/5x → error

Example: `java -cp target/gemini-2025.jar gemini.Server src/main/java/ServerDir 1058`


### Gemini Proxy Program

Command-line usage: `java -cp target/gemini-2025.jar gemini.Proxy <port>`.

Argument/s:
- `port` is required because the proxy needs to know which port to connect to

Valid formats: Anything that applies to the project specification (Gemini protocol).

Status Code returned to the `Client`:
- 43 → there was an error

Example: `java -cp target/gemini-2025.jar gemini.Proxy 9999`
