# Stage 1: Build the application
FROM node:20-alpine AS builder

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json (if exists) and install dependencies
COPY package*.json ./
RUN npm ci

# Copy source code and configuration files
COPY tsconfig.json ./
COPY src ./src

# Build TypeScript to JavaScript
RUN npm run build

# Stage 2: Create the runtime image
FROM node:20-alpine

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json for runtime dependencies
COPY package*.json ./
RUN npm ci --only=production

# Copy compiled JavaScript from builder stage
COPY --from=builder /app/dist ./dist

# Expose the application port
EXPOSE 3000

# Run the application
CMD ["node", "dist/index.js"]