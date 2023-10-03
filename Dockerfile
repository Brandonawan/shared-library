# Use Ubuntu as the base image
FROM ubuntu:latest

# Install any necessary packages
RUN apt-get update && apt-get install -y \
    python3-venv \
    python3-pip \
    pytest

# Create a virtual environment and activate it
RUN python3 -m venv venv
RUN . venv/bin/activate

# Create the 'jenkin-build' file (replace with your actual content)
RUN echo "This is a non-executable file content" > jenkin-build

# Install any additional dependencies if needed
# COPY requirements.txt /app/
# RUN pip install -r /app/requirements.txt

# Add your pipeline configuration file (pipeline-config.yml) if needed
# COPY pipeline-config.yml /app/

# Deactivate the virtual environment
RUN deactivate

# Define a command to run when the container starts
CMD ["bash"]
