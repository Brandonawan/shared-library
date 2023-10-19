# #!/bin/bash

# # Replace this with your custom checkout logic
# echo "Running custom checkout script..."

# git clone git@github.com:Brandonawan/shared-library.git
# # Add any other custom checkout steps here


# Define the checkout directory
CHECKOUT_DIR="${WORKSPACE}/your-checkout-directory"

# Create the checkout directory if it doesn't exist
mkdir -p "$CHECKOUT_DIR"

# Change to the checkout directory
cd "$CHECKOUT_DIR"

# Perform the custom checkout operation
git clone git@github.com:Brandonawan/shared-library.git

echo "Running custom checkout script..."

echo "cloning done"
# Add any other custom checkout steps here


