package krs.user

import com.twitter.util.{Future}

import krs.partner.api.{PartnerApi}
import krs.eligibility.api.{EligibilityApi}
import krs.partner.domain.{Offer}

case class UserWithOffers(
  user: User,
  offers: Seq[Offer]
)

case class UserApi(
    repository: UserRepository,
    partnerRepository: PartnerApi,
    eligibilitySystem: EligibilityApi
) {

  def getUser(id: Int): Option[User] = {
    UserSystem(repository).getUser(id)
  }

  def getUserWithOffers(id: Int): Future[Option[UserWithOffers]] = {
    UserSystem(repository).getUser(id) match {
      case Some(u) => {
        for {
          offers <- partnerRepository.getOffers(u.creditScore)
          eligible <- eligibilitySystem.filterEligible(u, offers)
        } yield Option(UserWithOffers(u, eligible))
      }
      case None => Future.value(None)
    }
  }
}

trait ApiModule { this: DomainModule =>
  val partnerRepository: PartnerApi
  val eligibilityApi: EligibilityApi
  val userApi = UserApi(repository, partnerRepository, eligibilityApi)
}
