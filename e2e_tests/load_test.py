import logging
import copy
import requests
import os
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import List, Tuple, Optional, Dict, Any
from get_user_token_to_test import get_user_token

# Configure logging
logger = logging.getLogger(__name__)

class LoadTestConfig:
    """Configuration class for load test parameters."""

    _REQUIRED_KC_KEYS = (
        "KC_URL",
        "KC_REALM",
        "KC_CLIENT_ID",
        "KC_CLIENT_SECRET",
        "KC_GRANT_TYPE",
        "KC_USERNAME",
        "KC_PASSWORD",
    )

    def __init__(self):
        host = os.getenv("APP_HOST", "http://localhost").rstrip("/")
        port = os.getenv("APP_PORT", "8090")
        app_url = f"{host}:{port}"

        self.customers_url = f"{app_url}/api/v1/shop/customers"
        self.products_url = f"{app_url}/api/v1/shop/products"

        kc_settings = self._resolve_keycloak_settings()

        user_token = get_user_token(
            kc_url=kc_settings["KC_URL"],
            realm=kc_settings["KC_REALM"],
            client_id=kc_settings["KC_CLIENT_ID"],
            client_secret=kc_settings["KC_CLIENT_SECRET"],
            grant_type=kc_settings["KC_GRANT_TYPE"],
            username=kc_settings["KC_USERNAME"],
            password=kc_settings["KC_PASSWORD"],
        )

        self.headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {user_token}"
        }
        
        # Default load parameters
        self.total_customers = int(os.getenv("LOAD_TOTAL_CUSTOMERS", "100"))
        self.concurrent_workers = int(os.getenv("LOAD_CONCURRENT_WORKERS", "20"))
        self.purchases_per_customer = int(os.getenv("LOAD_PURCHASES_PER_CUSTOMER", "2"))

    @classmethod
    def _resolve_keycloak_settings(cls) -> Dict[str, str]:
        """Resolves and validates all required Keycloak environment variables."""
        settings = {key: os.getenv(key) for key in cls._REQUIRED_KC_KEYS}
        missing = [key for key, value in settings.items() if not value]
        if missing:
            raise ValueError(
                f"Missing required Keycloak environment variables: {', '.join(missing)}"
            )
        return settings

class CustomerLoadService:
    """Service responsible for customer-related load operations."""
    
    def __init__(self, config: LoadTestConfig):
        self.config = config
        self.base_payload = {
            "firstName": "LoadTest",
            "lastName": "User",
            "birthDate": "1990-01-01",
            "address": {
                "street": "Test St",
                "number": "1",
                "postcode": "12345",
                "city": "LoadCity",
                "country": "Germany"
            },
            "invoiceAddress": None,
            "communicationDetails": {
                "email": "loadtest@example.com",
                "telephone": "+49 123456789"
            },
            "brand": "GMX",
            "password": "securePassword123"
        }

    def register_and_activate(self, index: int) -> Tuple[bool, Optional[str]]:
        """Registers a new customer and activates them immediately."""
        payload = copy.deepcopy(self.base_payload)
        timestamp = int(time.time() * 1000)
        payload["communicationDetails"]["email"] = f"loadtest_{timestamp}_{index}@example.com"
        payload["firstName"] += f"_{index}"
        
        try:
            # Step 1: Registration
            response = requests.post(
                self.config.customers_url, 
                headers=self.config.headers, 
                json=payload,
                timeout=30
            )
            if response.status_code != 201:
                return False, f"Registration failed with status {response.status_code}"
            
            customer_id = response.json().get("id")
            
            # Step 2: Activation (Required for purchases)
            activation_url = f"{self.config.customers_url}/{customer_id}/activate"
            act_response = requests.put(activation_url, headers=self.config.headers, timeout=10)
            if act_response.status_code != 204:
                return False, f"Activation failed with status {act_response.status_code} for customer {customer_id}"
                
            return True, customer_id
        except Exception as e:
            return False, str(e)

class PurchaseLoadService:
    """Service responsible for product-related load operations."""
    
    def __init__(self, config: LoadTestConfig):
        self.config = config

    def purchase_product(self, customer_id: str, product_id: str) -> int:
        """Simulates a product purchase for a given customer."""
        payload = {"productId": product_id}
        url = f"{self.config.customers_url}/{customer_id}/purchases"
        try:
            response = requests.post(
                url, 
                headers=self.config.headers, 
                json=payload,
                timeout=30
            )
            return response.status_code
        except Exception as e:
            logger.error(f"Purchase failed for customer {customer_id}: {e}")
            return 500

    def get_valid_product_id(self, brand: str = "GMX") -> str:
        """Retrieves a valid product ID from the catalog."""
        try:
            params = {"brand": brand}
            response = requests.get(self.config.products_url, params=params, timeout=5)
            if response.status_code == 200:
                products = response.json()
                if products:
                    return products[0]["id"]
            return "00000000-0000-0000-0000-000000000001"
        except Exception:
            return "00000000-0000-0000-0000-000000000001"

class LoadTestRunner:
    """Orchestrates the execution of load tests."""
    
    def __init__(self):
        self.config = LoadTestConfig()
        self.customer_service = CustomerLoadService(self.config)
        self.purchase_service = PurchaseLoadService(self.config)

    def run_load_test(self, total: int = None, workers: int = None, purchases: int = None):
        """Executes the complete load test cycle: registration, activation, and multiple purchases."""
        total = total or self.config.total_customers
        workers = workers or self.config.concurrent_workers
        purchases = purchases or self.config.purchases_per_customer
        
        logger.info(f"Starting Load Test: {total} customers, {workers} concurrent workers")
        
        customer_ids = self._register_customers(total, workers)
        logger.info(f"Phase 1 Complete: {len(customer_ids)} customers ready.")
        
        product_id = self.purchase_service.get_valid_product_id()
        successes, failures = self._perform_purchases(customer_ids, product_id, purchases, workers * 2)
        
        logger.info("=== Load Test Report ===")
        logger.info(f"Target Customers: {total}")
        logger.info(f"Successful Registrations: {len(customer_ids)}")
        logger.info(f"Successful Purchases: {successes}")
        logger.info(f"Failed Purchases: {failures}")
        
        return successes, failures

    def _register_customers(self, total: int, workers: int) -> List[str]:
        customer_ids = []
        with ThreadPoolExecutor(max_workers=workers) as executor:
            futures = {executor.submit(self.customer_service.register_and_activate, i): i for i in range(total)}
            for future in as_completed(futures):
                success, result = future.result()
                if success:
                    customer_ids.append(result)
                else:
                    logger.warning(f"Registration/Activation failed for task {futures[future]}: {result}")
        return customer_ids

    def _perform_purchases(self, customer_ids: List[str], product_id: str, count: int, workers: int) -> Tuple[int, int]:
        purchase_results = []
        with ThreadPoolExecutor(max_workers=workers) as executor:
            purchase_futures = []
            for customer_id in customer_ids:
                for _ in range(count):
                    purchase_futures.append(executor.submit(self.purchase_service.purchase_product, customer_id, product_id))
            
            for future in as_completed(purchase_futures):
                purchase_results.append(future.result())

        success_count = sum(1 for r in purchase_results if r in [201, 204, 409])
        failure_count = len(purchase_results) - success_count
        return success_count, failure_count

if __name__ == "__main__":
    # Configure root logger if this script is run standalone
    logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(name)s: %(message)s')
    
    runner = LoadTestRunner()
    # Using user-provided values in __main__
    runner.run_load_test(total=500, workers=50, purchases=10)
