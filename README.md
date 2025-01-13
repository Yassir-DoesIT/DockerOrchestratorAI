# Docker Swarm Orchestration with AI Auto-Scaling

## Overview
This project is a Java-based Docker Swarm orchestration tool designed to manage containerized services efficiently. It uses an M5P model (via WEKA) for AI-driven auto-scaling, with metrics collected from Prometheus and cAdvisor. The project features a JavaFX graphical interface, allowing users to:

- Enable or disable the AI agent for auto-scaling.
- View real-time logs of AI actions.
- Monitor active services in the Docker Swarm.
- Scale services manually if needed.

## Features
- **AI Auto-Scaling**: Automatically adjusts the number of replicas based on system load using an M5P model implemented with WEKA. The AI agent was trained using dummy data, making this project a learning exercise and proof-of-concept rather than a production-ready solution.
- **Monitoring Integration**: Retrieves metrics from Prometheus, which scrapes data about currently running services from a cAdvisor container.
- **JavaFX GUI**: Provides an interface for managing and monitoring services along with enabling or disabling the AI agent.
- **Manual Scaling**: Allows users to manually scale services.

### Notes
- The latency and request rate metrics displayed in the application are random placeholders and are not currently implemented. These features may not be developed in the future.
- There is a minor bug in the GUI: when selecting a service to scale, the program refreshes the service list every 5 seconds to fetch new metrics. This causes the selected service to be deselected if a refresh occurs during the selection process.
- To see the AI agent in action, you must artificially induce load on a service (e.g., by using a tool like `stress` or `hey`).

## Project Structure

### Utilities
- **`ModelTrainer`**: Trains the AI model.
- **`ResourceLoader`**: Loads the trained model.

### Main
- **`AIAgent`**: Implements the AI auto-scaling logic.
- **`DockerService`**: Manages Docker Swarm services.
- **`PrometheusClient`**: Interfaces with Prometheus to fetch metrics.
- **`ServiceStatus`**: Data class that contains attributes related to each currently running service; used to populate the GUI and tie it to the backend.

### GUI
- **`MainController`**: JavaFX GUI controller. Handles GUI events and queries `PrometheusClient` and `DockerService` to populate the GUI.
- **`MainApp`**: Application entry point. Creates and displays the primary application window, alongside loading the `main.fxml` file to initialize the GUI layout.

### Resources
- `replica-model.model`: Dummy trained AI model file.
- `training-data.csv`: Dummy data used for model training.
- `main.fxml`: JavaFX GUI layout.

### Tests
- **`AIAgentTest`**: Tests AI agent functionality using JUnit and Mockito.
- **`DockerServiceTest`**: Tests Docker integration using JUnit.
- **`PrometheusClientTest`**: Tests Prometheus integration using JUnit.

## Deployment

### Prerequisites
- Docker installed on a Linux distribution. Docker Desktop on Windows faces significant permission issues that are difficult to resolve, making it unsuitable for this project.
- If on Windows:
  - Install and run Docker on a WSL distribution.
  - Modify the `/etc/docker/daemon.json` file in WSL to include the following configuration:
    ```json
    {
      "hosts": [
        "unix:///var/run/docker.sock",
        "tcp://<WSL_IP>:2375"
      ]
    }
    ```
    Replace `<WSL_IP>` with the IP address of the WSL instance, which can be found using `hostname -I` or `ip addr` inside WSL.

    Ensure the IP is accessible from Windows. You can test this by pinging the IP from Windows.

    This will allow the Docker daemon to listen on port 2375 for remote connections from your Windows OS.

    **Note**: Exposing the Docker daemon over TCP is insecure unless secured with TLS or restricted to specific IPs.

  - Install the Docker CLI on Windows (e.g., via Chocolatey):
    ```bash
    choco install docker-cli
    ```

  - Configure the `DOCKER_HOST` environment variable on Windows to point to the Docker daemon in WSL:
    - Temporarily by running this command:
      ```cmd
      set DOCKER_HOST=tcp://127.0.0.1:2375
      ```
    - Or permanently by adding a new system environment variable.

- Prometheus and cAdvisor containers running outside the Docker Swarm. Deploying them inside the Swarm may result in permission and visibility issues (e.g., cAdvisor being unable to access metrics from other Swarm containers).
- Some services deployed in the Swarm.

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/Yassir-DoesIT/DockerOrchestratorAI.git
   ```
2. Set up Prometheus and cAdvisor as described in the [Prometheus Documentation](https://prometheus.io/docs/introduction/overview/).
3. Build and run the JavaFX application:
   ```bash
   mvn clean install
   mvn javafx:run
   ```

## Usage
1. Start the application.
2. View real-time metrics and logs in the GUI.
3. Monitor services and make manual adjustments as desired.
4. Enable the AI agent for automatic scaling.
5. Artificially induce load on a service to observe the AI agent in action.

## Development Process
This project was developed independently, focusing on:
1. Setting up a functional Docker Swarm environment in a WSL Ubuntu distro.
2. Configured the WSL Docker Swarm environment to allow communication with the Windows OS by enabling a TCP endpoint and setting up the Docker CLI on Windows.
3. Integrating Prometheus and cAdvisor for metrics collection.
4. Implementing the M5P model for decision-making using WEKA.
5. Creating a user-friendly JavaFX interface.
6. Writing unit tests for Prometheus and Docker integration using JUnit, and for the AI agent using JUnit and Mockito.

## License
This project is licensed under the [MIT License](LICENSE).

---
Feel free to use and adapt this project for your needs. Contributions are welcome!
