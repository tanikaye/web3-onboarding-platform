# Web3 Platform Wallet Service

A modern web application for managing cryptocurrency wallets and interacting with DeFi protocols.

## Prerequisites

- Node.js (v16 or higher)
- npm (v7 or higher)
- Java 17 or higher
- Maven 3.6 or higher

## Frontend Setup

1. Install dependencies:
   ```bash
   ./setup-frontend.sh
   ```

2. Start the development server:
   ```bash
   ./start-frontend.sh
   ```

The frontend development server will start at http://localhost:3000.

## Frontend Development

The frontend is built with:
- React 18
- Tailwind CSS
- WebSocket for real-time updates
- Web3.js and ethers.js for blockchain interactions

### Project Structure

```
src/main/resources/static/
├── js/
│   ├── App.js                 # Main application component
│   ├── components/            # React components
│   │   ├── Dashboard.js       # Main dashboard
│   │   ├── WalletCreation.js  # Wallet creation flow
│   │   └── defi/             # DeFi protocol interfaces
│   ├── hooks/                # Custom React hooks
│   └── utils/                # Utility functions
├── css/
│   └── main.css              # Main stylesheet with Tailwind
└── dist/                     # Built files
```

### Available Scripts

- `npm start`: Start the development server
- `npm run build`: Build for production
- `npm test`: Run tests
- `npm run lint`: Run ESLint
- `npm run format`: Format code with Prettier

## Backend Setup

1. Build the project:
   ```bash
   mvn clean install
   ```

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The backend server will start at http://localhost:8080.

## Features

- Wallet creation and management
- Real-time transaction monitoring
- DeFi protocol integration:
  - Uniswap for token swaps
  - Aave for lending and borrowing
  - OpenSea for NFT trading
- WebSocket-based real-time updates
- Secure authentication and authorization
- Responsive design with Tailwind CSS

## Development

### Frontend Development

1. Start the frontend development server:
   ```bash
   ./start-frontend.sh
   ```

2. The development server includes:
   - Hot module replacement
   - ESLint integration
   - Source maps
   - Proxy configuration for API and WebSocket

### Backend Development

1. Run the application in development mode:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. The development server includes:
   - Hot reload
   - Detailed error messages
   - H2 database for development

## Testing

### Frontend Tests

Run the frontend tests:
```bash
npm test
```

### Backend Tests

Run the backend tests:
```bash
mvn test
```

## Deployment

### Frontend Deployment

1. Build the frontend:
   ```bash
   npm run build
   ```

2. The built files will be in `src/main/resources/static/dist/`

### Backend Deployment

1. Build the application:
   ```bash
   mvn clean package
   ```

2. Run the JAR file:
   ```bash
   java -jar target/wallet-service-1.0.0.jar
   ```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.