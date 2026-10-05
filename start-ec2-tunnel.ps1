# ============================================================
# start-ec2-tunnel.ps1
# Starts an SSH tunnel to the EC2 PostgreSQL container and
# then launches the Docker services in cloud profile.
#
# Usage:
#   .\start-ec2-tunnel.ps1                  # tunnel + compose up
#   .\start-ec2-tunnel.ps1 -TunnelOnly      # only open the tunnel
#   .\start-ec2-tunnel.ps1 -Build           # tunnel + compose up --build
# ============================================================

param(
    [switch]$TunnelOnly,
    [switch]$Build
)

$EC2_USER = "ec2-user"
$EC2_HOST = "75.101.236.244"
$LOCAL_PORT = 5432
$REMOTE_PORT = 5432

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  One Enterprise Cloud - EC2 DB Tunnel  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Opening SSH tunnel:" -ForegroundColor Yellow
Write-Host "  localhost:$LOCAL_PORT  -->  $EC2_HOST (oec-postgres:$REMOTE_PORT)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Credentials: $EC2_USER @ $EC2_HOST" -ForegroundColor Gray
Write-Host "Enter password when prompted (DevOps321) or use your .pem key." -ForegroundColor Gray
Write-Host ""

# Start tunnel in background job
$tunnelCmd = "ssh -N -o StrictHostKeyChecking=accept-new -L ${LOCAL_PORT}:localhost:${REMOTE_PORT} ${EC2_USER}@${EC2_HOST}"
Write-Host "Running: $tunnelCmd" -ForegroundColor DarkGray

$tunnelJob = Start-Job -ScriptBlock {
    param($cmd)
    Invoke-Expression $cmd
} -ArgumentList $tunnelCmd

Write-Host ""
Write-Host "Waiting 5 seconds for tunnel to establish..." -ForegroundColor Gray
Start-Sleep -Seconds 5

# Check if tunnel job is still alive
if ($tunnelJob.State -eq "Running") {
    Write-Host "Tunnel is active (Job ID: $($tunnelJob.Id))" -ForegroundColor Green
} else {
    Write-Host "WARNING: Tunnel job may have failed. Check SSH output below:" -ForegroundColor Red
    Receive-Job $tunnelJob
}

if (-not $TunnelOnly) {
    Write-Host ""
    Write-Host "Starting Docker services with cloud profile..." -ForegroundColor Cyan

    $composeArgs = @("compose", "--profile", "cloud", "up")
    if ($Build) { $composeArgs += "--build" }

    Write-Host "Running: docker $($composeArgs -join ' ')" -ForegroundColor DarkGray
    Write-Host ""

    try {
        & docker @composeArgs
    } finally {
        Write-Host ""
        Write-Host "Stopping SSH tunnel (Job ID: $($tunnelJob.Id))..." -ForegroundColor Yellow
        Stop-Job $tunnelJob
        Remove-Job $tunnelJob
        Write-Host "Tunnel closed." -ForegroundColor Green
    }
} else {
    Write-Host ""
    Write-Host "Tunnel is running in the background (Job ID: $($tunnelJob.Id))." -ForegroundColor Green
    Write-Host "To stop it, run:  Stop-Job $($tunnelJob.Id); Remove-Job $($tunnelJob.Id)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Now you can run:  docker compose --profile cloud up --build" -ForegroundColor Yellow
}
