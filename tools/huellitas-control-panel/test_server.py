import unittest

from server import systemctl_command


class SystemdCommandTests(unittest.TestCase):
    def test_only_fixed_service_units_are_targeted(self):
        self.assertEqual(
            systemctl_command("spring", "start"),
            ["systemctl", "--user", "start", "huellitas-spring.service"],
        )
        self.assertEqual(
            systemctl_command("angular", "stop"),
            ["systemctl", "--user", "stop", "huellitas-angular.service"],
        )

    def test_rejects_arbitrary_service_and_action_strings(self):
        for service, action in [
            ("spring; touch /tmp/pwned", "start"),
            ("spring", "start; rm -rf /"),
            ("system", "restart"),
        ]:
            with self.subTest(service=service, action=action):
                with self.assertRaises(ValueError):
                    systemctl_command(service, action)


if __name__ == "__main__":
    unittest.main()
