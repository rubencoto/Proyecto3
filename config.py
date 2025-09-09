from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    # Database Configuration
    db_host: str = "localhost"
    db_port: int = 3306
    db_user: str = "banking_user"
    db_pass: str = "banking_password"
    db_name: str = "banking_mvp"

    # Application Configuration
    app_host: str = "0.0.0.0"
    app_port: int = 8000
    debug: bool = True

    # Logging
    log_level: str = "INFO"

    @property
    def database_url(self) -> str:
        return f"mysql+pymysql://{self.db_user}:{self.db_pass}@{self.db_host}:{self.db_port}/{self.db_name}?ssl_mode=REQUIRED"

    class Config:
        env_file = ".env"


settings = Settings()