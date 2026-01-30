#!/bin/bash

# Configuration
DB_NAME="smartpos"
DB_USER="postgres"
DB_PASS="password"

echo "🐘 PostgreSQL Database Setup for SmartPOS..."

# Check if postgres is running
if ! pgrep -x "postgres" > /dev/null; then
    echo "❌ PostgreSQL is not running!"
    echo "Please start it with: sudo service postgresql start"
    exit 1
fi

# Create Database (if not exists)
if psql -U $DB_USER -lqt | cut -d \| -f 1 | grep -qw $DB_NAME; then
    echo "✅ Database '$DB_NAME' already exists."
else
    echo "⚙️ Creating database '$DB_NAME'..."
    createdb -U $DB_USER $DB_NAME
    if [ $? -eq 0 ]; then
        echo "✅ Database '$DB_NAME' created successfully!"
    else
        echo "❌ Failed to create database. Check permissions."
        exit 1
    fi
fi

echo "🎉 Setup Complete! You can now run the app with ./run.sh"
echo "Note: Ensure your application.properties password matches your postgres user password."
