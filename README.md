# Contacts_api
Features a single endpoint: `POST /contacts/storeContact`
- stores contact information into a CSV file, if it does not exist yet.
- requireded query parameters:
  - `firstName` - contact's first name (cannot be empty)
  - `lastName` - contact's last name (cannot be empty)
  - `email` - contact's email (must be a valid email value)
