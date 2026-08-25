import requests
import copy
import logging

logger = logging.getLogger(__name__)


def test_register_customer_success(valid_customer_payload, base_url, header=None):
    response = requests.post(base_url, headers=header, json=valid_customer_payload)
    assert response.status_code == 201, f"Expected 201, got {response.status_code}"
    data = response.json()
    assert data.get("status") == "INACTIVE", "Customer status should be INACTIVE"
    print("Customer registration success passed")
    return data["id"]


def test_register_customer_missing_firstname(base_url, header, valid_customer_payload):

    missing_firstname_payload = copy.deepcopy(valid_customer_payload)
    del missing_firstname_payload["firstName"]

    response = requests.post(base_url, headers=header, json=missing_firstname_payload)
    assert response.status_code == 400, f"Expected 400, got {response.status_code}"
    print("Customer registration missing firstname passed")


def test_register_customer_missing_lastname(base_url, header, valid_customer_payload):

    missing_lastname_payload = copy.deepcopy(valid_customer_payload)
    del missing_lastname_payload["lastName"]

    response = requests.post(base_url, headers=header, json=missing_lastname_payload)
    assert response.status_code == 400, f"Expected 400, got {response.status_code}"
    print("Customer registration missing lastname passed")


def test_firstname_blank(base_url, header, valid_customer_payload):

    firstname_blank_payload = copy.deepcopy(valid_customer_payload)
    firstname_blank_payload["firstName"] = ""

    response = requests.post(base_url, headers=header, json=firstname_blank_payload)
    assert response.status_code == 400, f"Expected 400, got {response.status_code}"
    print("Customer registration firstname blank passed")


def test_lastname_blank(base_url, header, valid_customer_payload):

    lastname_blank_payload = copy.deepcopy(valid_customer_payload)
    lastname_blank_payload["lastName"] = ""

    response = requests.post(base_url, headers=header, json=lastname_blank_payload)
    assert response.status_code == 400, f"Expected 400, got {response.status_code}"
    print("Customer registration lastname blank passed")

def test_firstname_max_length(base_url, header, valid_customer_payload):

    firstname_max_length_payload = copy.deepcopy(valid_customer_payload)
    firstname_max_length_payload["firstName"] = "a" * 101

    response = requests.post(base_url, headers=header, json=firstname_max_length_payload)
    assert response.status_code == 400, f"Expected 400, got {response.status_code}"
    print("Customer registration firstname max length passed")

def test_lastname_max_length(base_url, header, valid_customer_payload):

    lastname_max_length_payload = copy.deepcopy(valid_customer_payload)
    lastname_max_length_payload["lastName"] = "a" * 101

    response = requests.post(base_url, headers=header, json=lastname_max_length_payload)
    assert response.status_code == 400, f"Expected 400, got {response.status_code}"
    print("Customer registration lastname max length passed")

def test_register_customer_invalid_email(base_url, header, valid_customer_payload):

    invalid_email_payload = copy.deepcopy(valid_customer_payload)
    invalid_email_payload["communicationDetails"]["email"] = "invalid-email"

    response = requests.post(base_url, headers=header, json=invalid_email_payload)
    assert response.status_code == 400, f"Expected 400, got {response.status_code}"
    print("Customer registration invalid email passed")


def test_email_max_length(base_url, header, valid_customer_payload):
    email_max_length_payload = copy.deepcopy(valid_customer_payload)
    
    email_max_length_payload["communicationDetails"]["email"] = "a" * 250 + "@test.com"

    response = requests.post(base_url, headers=header, json=email_max_length_payload)
    assert response.status_code == 400, f"Expected 400 for long email, got {response.status_code}"
    print("Customer registration email max length passed")


def test_email_unicode_special_chars(base_url, header, valid_customer_payload):
    unicode_email_payload = copy.deepcopy(valid_customer_payload)
    unicode_email_payload["communicationDetails"]["email"] = "тест.email+special!#@example.com"

    response = requests.post(base_url, headers=header, json=unicode_email_payload)
    
    assert response.status_code == 400, f"Expected 400 for special/unicode email, got {response.status_code}"
    print("Customer registration email unicode/special chars passed")

def test_register_customer_null_fields(base_url, header, valid_customer_payload):
    fields_to_test = ["birthDate", "address", "communicationDetails", "brand"]
    for field in fields_to_test:
        null_payload = copy.deepcopy(valid_customer_payload)
        null_payload[field] = None
        
        response = requests.post(base_url, headers=header, json=null_payload)
        assert response.status_code == 400, f"Expected 400 for null {field}, got {response.status_code}"
        print(f"Customer registration null {field} passed")


def test_get_customer_success(customer_id, base_url, header=None):
    url = f"{base_url}/{customer_id}"
    response = requests.get(url, headers=header)
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"
    print("Get customer success passed")

def test_get_customer_not_found(base_url, invalid_customer_id, header=None):
    url = f"{base_url}/{invalid_customer_id}"
    response = requests.get(url, headers=header)
    assert response.status_code == 404, f"Expected 404, got {response.status_code}"
    print("Get customer not found passed")


def test_update_address_success(customer_id, base_url, header, payload):
    url = f"{base_url}/{customer_id}/address"

    update_address_payload = {
        "street": "Neustraße",
        "number": "5",
        "postcode": "10115",
        "city": "Berlin",
        "country": "Germany"
    }

    response = requests.put(url, headers=header, json=update_address_payload)
    assert response.status_code == 204, f"Expected 204, got {response.status_code}"
    print("Update address success passed")


def test_update_address_invalid(customer_id, base_url, header, valid_customer_payload):
    url = f"{base_url}/{customer_id}/address"
    
    invalid_address_payload = {
        "street": "",
        "number": "5",
        "postcode": "10115",
        "city": "Berlin",
        "country": "Germany"
    }

    response = requests.put(url, headers=header, json=invalid_address_payload)
    assert response.status_code == 400, f"Expected 400, got {response.status_code}"
    print("Update address invalid data passed")


def test_deactivate_customer(customer_id, base_url, header=None):
    url_deactivate = f"{base_url}/{customer_id}/deactivate"
    response = requests.put(url_deactivate, headers=header)
    assert response.status_code == 204, f"Expected 204, got {response.status_code}"
    print("Customer deactivation passed")

def test_activate_customer(customer_id, base_url, header=None):
    url_activate = f"{base_url}/{customer_id}/activate"
    response = requests.put(url_activate, headers=header)
    assert response.status_code == 204, f"Expected 204, got {response.status_code}"
    print("Customer activation passed")

def test_activate_customer_idempotency(customer_id, base_url, header=None):
    url_activate = f"{base_url}/{customer_id}/activate"
    
    response = requests.put(url_activate, headers=header)
    assert response.status_code in [204, 409], f"Idempotency check failed: expected 204 or 409, got {response.status_code}"
    
    url_get = f"{base_url}/{customer_id}"
    get_res = requests.get(url_get, headers=header)
    assert get_res.json()["status"] == "ACTIVE", "Customer status changed unexpectedly"
    print("Customer activation idempotency check passed")

def test_update_address_inactive_fail(customer_id, base_url, header):

    
    url = f"{base_url}/{customer_id}/address"
    payload = {
        "street": "Forbidden St",
        "number": "1",
        "postcode": "00000",
        "city": "FailCity",
        "country": "Germany"
    }
    response = requests.put(url, headers=header, json=payload)
    assert response.status_code in [400, 403, 409], f"Should fail to update inactive customer, got {response.status_code}"
    print("Update address for inactive customer failure (expected) passed")

def test_purchase_customer_inactive_fail(customer_id, product_id, base_url, header):
    url = f"{base_url}/{customer_id}/purchases"
    payload = {"productId": product_id}
    response = requests.post(url, headers=header, json=payload)
    assert response.status_code in [400, 403, 409], f"Should fail to purchase for inactive customer, got {response.status_code}"
    print("Purchase for inactive customer failure (expected) passed")

def test_delete_active_customer_fail(customer_id, base_url, header=None):
    url = f"{base_url}/{customer_id}"
    response = requests.delete(url, headers=header)
    assert response.status_code in [400, 409], f"Should fail to delete active customer with contracts, got {response.status_code}"
    print("Delete active customer with contracts failure (expected) passed")


def test_update_communication_details(customer_id, base_url, header, payload):
    url = f"{base_url}/{customer_id}/communication-details"

    update_communication_payload = {
        "email": "new.email@gmx.de",
        "telephone": "+49 621 654321"
    }
    response = requests.put(url, headers=header, json=update_communication_payload)
    assert response.status_code == 204, f"Expected 204, got {response.status_code}"
    print("Update communication details passed")

def get_valid_product_id(products_url, brand="GMX"):
    params = {"brand": brand}
    response = requests.get(products_url, params=params)
    assert response.status_code == 200, "Should be able to fetch products"
    products = response.json()
    assert len(products) > 0, f"No products found for brand {brand}"
    return products[0]["id"]


def test_should_purchase_product_successfully_when_valid_data_provided(customer_id, product_id, base_url, header):
    """Test that the API correctly processes a product purchase for an active customer."""
    url = f"{base_url}/{customer_id}/purchases"
    payload = {"productId": product_id}
    response = requests.post(url, headers=header, json=payload)
    assert response.status_code == 201, f"Expected 201, got {response.status_code}"
    logger.info(f"Test Success: Product {product_id} purchased by customer {customer_id}.")

def test_should_handle_product_purchase_idempotently_when_called_multiple_times(customer_id, product_id, base_url, header):
    """Verify that repeated purchase requests for the same product are handled idempotently."""
    url = f"{base_url}/{customer_id}/purchases"
    payload = {"productId": product_id}
    
    contracts_url = base_url.replace("/customers", "/contracts") + f"/{customer_id}"
    c1 = requests.get(contracts_url).json()

    response = requests.post(url, headers=header, json=payload)
    assert response.status_code in [201, 409, 204], f"Purchase idempotency failed, got {response.status_code}"

    c2 = requests.get(contracts_url).json()
    assert len(c1) == len(c2), f"Duplicate contract created! Count increased from {len(c1)} to {len(c2)}"
    logger.info("Test Success: Product purchase idempotency check passed.")

def test_should_verify_purchased_product_data_integrity_after_purchase(customer_id, product_id, base_url, header):
    """Verify that the contract returned after a purchase has the correct customer, product, and initial status."""
    url = f"{base_url}/{customer_id}/purchases"
    payload = {"productId": product_id}
    response = requests.post(url, headers=header, json=payload)
    assert response.status_code == 201, f"Expected 201, got {response.status_code}"
    
    contract = response.json()
    assert contract["customerId"] == customer_id, "Wrong customerId in contract"
    assert contract["productId"] == product_id, "Wrong productId in contract"
    assert "id" in contract, "Missing contract ID"
    assert "creationDate" in contract, "Missing creation date"
    assert contract["status"] == "INACTIVE", "New contract should be INACTIVE"
    logger.info("Test Success: Product purchase data verification passed.")


def test_should_successfully_delete_customer_when_id_is_valid(customer_id, base_url, header=None):
    """Verify that a customer can be successfully deleted when they have no active contracts."""
    url = f"{base_url}/{customer_id}"
    response = requests.delete(url, headers=header)
    assert response.status_code == 204, f"Expected 204, got {response.status_code}"
    logger.info(f"Test Success: Customer {customer_id} deleted successfully.")


def test_get_current_customer_me_success(base_url_shop, header, expected_customer_id=None):
    """Verify GET /api/v1/shop/customers/me returns current customer profile when authenticated."""
    url = f"{base_url_shop}/customers/me"
    response = requests.get(url, headers=header)
    assert response.status_code == 200, f"Expected 200, got {response.status_code}: {response.text}"
    data = response.json()
    assert "id" in data, "Response should contain customer ID"
    assert "firstName" in data, "Response should contain firstName"
    assert "lastName" in data, "Response should contain lastName"
    assert "brand" in data, "Response should contain brand"
    assert "communicationDetails" in data, "Response should contain communicationDetails"
    if expected_customer_id:
        assert data["id"] == expected_customer_id, f"Expected customer ID {expected_customer_id}, got {data['id']}"
    logger.info("Test Success: GET /api/v1/shop/customers/me returned current authenticated customer profile.")
    print("Get current customer me success passed")
    return data


def test_get_current_customer_me_unauthorized(base_url_shop):
    """Verify GET /api/v1/shop/customers/me returns 401/403 when unauthenticated."""
    url = f"{base_url_shop}/customers/me"
    unauth_headers = {"Content-Type": "application/json"}
    response = requests.get(url, headers=unauth_headers)
    assert response.status_code in [401, 403], f"Expected 401 or 403 for unauthenticated request, got {response.status_code}"
    logger.info("Test Success: GET /api/v1/shop/customers/me correctly rejected unauthenticated request.")
    print("Get current customer me unauthorized passed")


def test_get_current_customer_me_not_found(base_url_shop, header):
    """Verify GET /api/v1/shop/customers/me returns 404 when authenticated user has no matching customer profile."""
    url = f"{base_url_shop}/customers/me"
    response = requests.get(url, headers=header)
    assert response.status_code == 404, f"Expected 404, got {response.status_code}: {response.text}"
    data = response.json()
    assert data.get("errorCode") == "CUSTOMER_NOT_FOUND", f"Expected CUSTOMER_NOT_FOUND error code, got {data}"
    logger.info("Test Success: GET /api/v1/shop/customers/me correctly returned 404 for unmapped user.")
    print("Get current customer me not found passed")