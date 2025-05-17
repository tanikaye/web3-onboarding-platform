#!/bin/bash

# Exit on error
set -e

echo "Setting up frontend development environment..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "Node.js is not installed. Please install Node.js first."
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "npm is not installed. Please install npm first."
    exit 1
fi

# Install dependencies
echo "Installing dependencies..."
npm install

# Install additional dependencies for Tailwind CSS
echo "Installing Tailwind CSS dependencies..."
npm install -D @tailwindcss/forms

# Create necessary directories
echo "Creating necessary directories..."
mkdir -p src/main/resources/static/dist

# Build the project
echo "Building the project..."
npm run build

echo "Frontend setup completed successfully!"