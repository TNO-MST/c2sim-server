# -----------------
# PowerShell script to create a virtual environment and install MkDocs + Material theme

Write-Host "Setting up MkDocs environment..." -ForegroundColor Cyan

# 1. Check Python installation
if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Host "Python not found. Please install Python and add it to PATH." -ForegroundColor Red
    exit 1
}

# 2. Create virtual environment
if (-not (Test-Path ".venv")) {
    Write-Host "Creating virtual environment (.venv)..."
    python -m venv .venv
} else {
    Write-Host "Virtual environment already exists, skipping creation."
}

# 3. Activate the environment
Write-Host "Activating virtual environment..."
& .\.venv\Scripts\Activate.ps1

# 4. Upgrade pip
Write-Host "Upgrading pip..."
pip install --upgrade pip

# 5. Install MkDocs and Material theme
Write-Host "Installing MkDocs and MkDocs-Material..."
pip install mkdocs mkdocs-material

# 6. Build doc
Write-Host "Creating doc folder for C2SIm server..."
cd ..
mkdocs build 

Write-Host ""
Write-Host "MKDOCS completed!" -ForegroundColor Green


mkdocs serve -a 0.0.0.0:1234