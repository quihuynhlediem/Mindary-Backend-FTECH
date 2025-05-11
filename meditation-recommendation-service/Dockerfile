# Stage 1: Build the application
FROM node:20-alpine AS builder
WORKDIR /app

# Copy package.json and install dependencies
COPY package*.json ./
RUN npm install --omit=dev

# Install Babel globally to prevent "babel not found" errors
RUN npm install --save-dev @babel/cli @babel/core @babel/preset-env

# Copy source files and build
COPY . /app
RUN npx babel src -d dist  # Ensure Babel runs with npx

# Stage 2: Create the runtime image
FROM node:20-alpine
WORKDIR /app

# Copy dependencies for runtime
COPY package*.json ./
RUN npm install --omit=dev

# Copy built files from builder stage
COPY --from=builder /app .

EXPOSE 8084
CMD ["node", "/app/server.js"]