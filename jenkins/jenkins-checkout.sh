# #!/bin/bash

# # Replace this with your custom checkout logic
# echo "Running custom checkout script..."

# git clone git@github.com:Brandonawan/shared-library.git
# # Add any other custom checkout steps here


#!/bin/bash

# Get the current workspace directory
WORKSPACE=`pwd`

# Change to the specified checkout directory
cd "${WORKSPACE}/your-checkout-directory"

# Perform the custom checkout operation
git clone git@github.com:Brandonawan/shared-library.git

# Add any other custom checkout steps here

