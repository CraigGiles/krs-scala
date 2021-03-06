package krs.user

import com.twitter.util.Future
import krs.eligibility.EligibilityApi
import krs.partner.PartnerApi

object UserDomain {
  import krs.partner.PartnerDomain._

  case class User(
    id: Int,
    name: String,
    creditScore: Int,
    outstandingLoanAmount: Double)

  case class UserWithOffers(
    user: User,
    offers: Seq[Offer]
  )

  case class UserNotFound(id: Int) extends Exception {
    override def getMessage: String = s"User(${id.toString}) not found."
  }
}

trait UserRepository {
  import UserDomain._

  def loadUsers(): List[User]
}

case class UserSystem(repository: UserRepository, partnerRepository: PartnerApi,
                      eligibilitySystem: EligibilityApi) {
  import UserDomain._

  def getUsers(): List[User] = {
    repository.loadUsers()
  }

  def getUser(id: Int): Option[User] = {
    repository.loadUsers().find(_.id == id)
  }

  def getUserWithOffers(id: Int): Future[Option[UserWithOffers]] = {
    getUser(id) match {
      case Some(u) =>
        for {
          offers <- partnerRepository.getOffers(u.creditScore)
          eligible <- eligibilitySystem.filterEligible(u, offers)
        } yield Option(UserWithOffers(u, eligible))
      case None => Future.value(None)
    }
  }
}

trait DomainModule {
  def repository: UserRepository
  val partnerRepository: PartnerApi
  val eligibilityApi: EligibilityApi
  val userApi = UserSystem(repository, partnerRepository, eligibilityApi)
}
