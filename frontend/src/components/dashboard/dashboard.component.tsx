import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Product, ForecastResult } from '../../models/inventory.models';
import * as api from '../../services/api.service';

interface Stats {
  totalProducts: number;
  lowStockCount: number;
  activeForecasts: number;
  totalValue: number;
}

function DashboardComponent() {
  const [stats, setStats] = useState<Stats>({
    totalProducts: 0,
    lowStockCount: 0,
    activeForecasts: 0,
    totalValue: 0,
  });
  const [lowStockProducts, setLowStockProducts] = useState<Product[]>([]);
  const [recentForecasts, setRecentForecasts] = useState<ForecastResult[]>([]);

  useEffect(() => {
    loadDashboardData();
  }, []);

  async function loadDashboardData() {
    const products = await api.getAllProducts().catch(() => []);
    setStats((prev) => ({
      ...prev,
      totalProducts: products.length,
      totalValue: products.reduce((sum, p) => sum + p.price * p.currentStock, 0),
      lowStockCount: products.filter(
        (p) => p.currentStock <= (p.minStockThreshold || 10)
      ).length,
    }));
    setLowStockProducts(
      products.filter((p) => p.currentStock <= (p.minStockThreshold || 10))
    );

    const forecastPromises = products.slice(0, 5).map((p) =>
      api.getLatestForecast(p.id!).catch(() => null)
    );
    const results = await Promise.all(forecastPromises);
    const validForecasts = results.filter(
      (f): f is ForecastResult => f !== null
    );
    setRecentForecasts(validForecasts);
    setStats((prev) => ({
      ...prev,
      activeForecasts: validForecasts.length,
    }));
  }

  return (
    <div className="container-fluid py-4">
      <div className="row mb-4">
        <div className="col-12">
          <h1 className="h3 mb-0">Dashboard</h1>
          <p className="text-muted">Overview of your inventory and forecasts</p>
        </div>
      </div>

      <div className="row mb-4">
        <div className="col-xl-3 col-md-6 mb-3">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body">
              <div className="d-flex justify-content-between">
                <div>
                  <h6 className="text-muted text-uppercase mb-1">Total Products</h6>
                  <h2 className="mb-0">{stats.totalProducts}</h2>
                </div>
                <div className="align-self-center text-primary">
                  <i className="bi bi-box-seam display-4"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-xl-3 col-md-6 mb-3">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body">
              <div className="d-flex justify-content-between">
                <div>
                  <h6 className="text-muted text-uppercase mb-1">Low Stock Alerts</h6>
                  <h2 className="mb-0 text-warning">{stats.lowStockCount}</h2>
                </div>
                <div className="align-self-center text-warning">
                  <i className="bi bi-exclamation-triangle display-4"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-xl-3 col-md-6 mb-3">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body">
              <div className="d-flex justify-content-between">
                <div>
                  <h6 className="text-muted text-uppercase mb-1">Active Forecasts</h6>
                  <h2 className="mb-0">{stats.activeForecasts}</h2>
                </div>
                <div className="align-self-center text-success">
                  <i className="bi bi-graph-up display-4"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-xl-3 col-md-6 mb-3">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body">
              <div className="d-flex justify-content-between">
                <div>
                  <h6 className="text-muted text-uppercase mb-1">Total Inventory Value</h6>
                  <h2 className="mb-0">${stats.totalValue.toFixed(2)}</h2>
                </div>
                <div className="align-self-center text-info">
                  <i className="bi bi-currency-dollar display-4"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-lg-8 mb-4">
          <div className="card border-0 shadow-sm">
            <div className="card-header bg-white border-0">
              <h5 className="mb-0">Low Stock Products</h5>
            </div>
            <div className="card-body p-0">
              <div className="table-responsive">
                <table className="table table-hover mb-0">
                  <thead className="table-light">
                    <tr>
                      <th>Product</th>
                      <th>Current Stock</th>
                      <th>Threshold</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {lowStockProducts.map((product) => (
                      <tr key={product.id}>
                        <td>
                          <div className="fw-medium">{product.name}</div>
                          <small className="text-muted">
                            {product.description || ''}
                          </small>
                        </td>
                        <td>{product.currentStock}</td>
                        <td>{product.minStockThreshold}</td>
                        <td>
                          <span className="badge bg-danger">Critical</span>
                        </td>
                        <td>
                          <button
                            className="btn btn-sm btn-outline-primary"
                            onClick={() => {}}
                          >
                            <i className="bi bi-pencil me-1"></i>Restock
                          </button>
                        </td>
                      </tr>
                    ))}
                    {lowStockProducts.length === 0 && (
                      <tr>
                        <td colSpan={5} className="text-center py-4 text-muted">
                          No low stock products
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
        <div className="col-lg-4 mb-4">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-header bg-white border-0 d-flex justify-content-between align-items-center">
              <h5 className="mb-0">Recent Forecasts</h5>
              <Link to="/forecasts" className="btn btn-sm btn-link p-0">
                View All
              </Link>
            </div>
            <div className="card-body p-0">
              <div className="list-group list-group-flush">
                {recentForecasts.map((forecast) => (
                  <Link
                    key={forecast.id}
                    to="/forecasts"
                    className="list-group-item list-group-item-action"
                  >
                    <div className="d-flex w-100 justify-content-between">
                      <h6 className="mb-1">{forecast.product?.name || ''}</h6>
                      <small>
                        {new Date(forecast.forecastDate).toLocaleDateString()}
                      </small>
                    </div>
                    <p className="mb-1">
                      Predicted Demand: <strong>{forecast.predictedDemand}</strong>{' '}
                      units
                    </p>
                    <small className="text-muted">
                      Model: {forecast.modelUsed || '-'} • Accuracy: {forecast.accuracyScore ? Math.round(forecast.accuracyScore * 100) : 0}%
                    </small>
                  </Link>
                ))}
                {recentForecasts.length === 0 && (
                  <div className="list-group-item text-center text-muted py-3">
                    No forecasts generated yet
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default DashboardComponent;
