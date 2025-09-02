#!/bin/bash

# Setup script for SQL Generator Service test environment
echo "Setting up test environment for SQL Generator Service..."

# Check if OPENAI_API_KEY is already set
if [ -n "$OPENAI_API_KEY" ]; then
    echo "✅ OPENAI_API_KEY is already set: ${OPENAI_API_KEY:0:20}..."
else
    echo "❌ OPENAI_API_KEY is not set"
    echo ""
    echo "To fix the quota issue, you need to:"
    echo "1. Get a new OpenAI API key from: https://platform.openai.com/api-keys"
    echo "2. Set the environment variable:"
    echo "   export OPENAI_API_KEY='your-new-api-key-here'"
    echo ""
    echo "3. Or add it to your shell profile (~/.bashrc, ~/.zshrc, etc.):"
    echo "   echo \"export OPENAI_API_KEY='your-new-api-key-here'\" >> ~/.zshrc"
    echo "   source ~/.zshrc"
    echo ""
    echo "4. Then run the tests again:"
    echo "   mvn test"
    echo ""
    exit 1
fi

# Check if the key looks valid
if [[ "$OPENAI_API_KEY" == sk-* ]]; then
    echo "✅ API key format looks valid"
else
    echo "⚠️  API key format doesn't look like a standard OpenAI key"
fi

echo ""
echo "Current configuration:"
echo "  OPENAI_API_KEY: ${OPENAI_API_KEY:0:20}..."
echo "  OPENAI_MODEL: ${OPENAI_MODEL:-gpt-3.5-turbo}"
echo "  OPENAI_TIMEOUT: ${OPENAI_TIMEOUT:-60}"
echo ""
echo "You can now run the tests:"
echo "  mvn test"
echo ""
echo "Or run specific tests:"
echo "  mvn test -Dtest=SqlGeneratorServiceIntegrationTest"
