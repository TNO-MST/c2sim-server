# --- Build Stage ---
FROM node:20-alpine AS builder

# Create working directory
WORKDIR /app

# Copy package and lockfile
COPY package.json ./

# Install dependencies
RUN npm install

# Copy all source files
COPY src ./src
COPY tsconfig.json .

# Compile TS -> JS
RUN npm run build

# --- Runtime Stage ---
FROM node:20-alpine

# Create app directory
WORKDIR /app

# Copy compiled output (JavaScript) from builder
COPY --from=builder /app/dist ./dist

# Copy production dependencies
COPY --from=builder /app/node_modules ./node_modules

# Start the app
CMD ["node", "dist/index.js"]
